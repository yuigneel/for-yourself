package com.yuigneel.common.interceptors;

import com.yuigneel.common.events.UserStatusCheckEvent;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import com.yuigneel.common.model.result.ResultCodeEnum;
import com.yuigneel.common.utils.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * 用户账号状态校验拦截器
 * <p>核心功能：通过发布 Spring 事件实现跨模块解耦的动态封禁校验</p>
 * <P>代码鲁棒性： 其实采用的是微服务，所以根本没必要区分身份这一逻辑，但为了方便以后改动，增加了这一个</P>
 */
@Slf4j
@RequiredArgsConstructor
public class UserStatusInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisher eventPublisher; // 事件发布器

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // yuigneel： 此处无需校验白名单，在注册器中已经配置了白名单和内部调用白名单

        // 1. 获取当前请求用户的 UID
        Long uid = UserContextUtil.getUid();
        if (uid == null) {
            // 如果 UID 为空，说明认证拦截器被异常击穿或未正确配置
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, "认证拦截器被异常击穿");
        }

        // 2. 判断身份类型（根据 URL 前缀区分）鲁棒性
        AccountIdentityTypeEnum identityType;
        if (uri.startsWith("/center-admin/")) {
            identityType = AccountIdentityTypeEnum.ADMIN;
        } else if (uri.startsWith("/center-common/")) {
            identityType = AccountIdentityTypeEnum.USER;
        } else {
            // 其他路径（如文档、健康检查等）直接放行
            return true;
        }

        // 3. 发布状态查询事件（同步阻塞，等待监听器回填结果）
        UserStatusCheckEvent event = new UserStatusCheckEvent(this, uid, identityType);
        eventPublisher.publishEvent(event);

        // 4. 校验结果
        if (Boolean.TRUE.equals(event.getIsBanned())) {
            // Yu·Igneel：懒得改了，能跑就行目前业务没有拓展，以后如果要优化封禁的话可以改一下事件逻辑
            LocalDateTime expireTime = event.getExpireTime();
            if (expireTime == null) {
                log.warn("用户 {} 已被强制销号，拦截请求: {}", uid, uri);
                throw new ForYourselfException(ResultCodeEnum.ACCOUNT_BANNED, "账号已被强制销号");
            } else {
                log.warn("用户 {} 已被封禁，拦截请求: {}", uid, uri);
                throw new ForYourselfException(ResultCodeEnum.ACCOUNT_BANNED,
                        "账号已被封禁，解封时间: " + event.getExpireTime());
            }
        }
        return true;
    }

}
