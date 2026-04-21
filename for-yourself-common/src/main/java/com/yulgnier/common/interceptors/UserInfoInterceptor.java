package com.yulgnier.common.interceptors;

import com.yulgnier.common.config.properties.TruthProperties;
import com.yulgnier.common.exception.ForYourselfException;
import com.yulgnier.common.model.constants.AuthConstants;
import com.yulgnier.common.model.result.ResultCodeEnum;
import com.yulgnier.common.utils.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class UserInfoInterceptor implements HandlerInterceptor {
    private final TruthProperties truthProperties;

    /**
     * 拦截器，在请求处理之前执行
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true: 放行，false: 拦截
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 看header里面是否有truth，且内容符合，没有返回错误
        String truth = request.getHeader(truthProperties.getTruthKey());
        if (truth == null || !truth.equals(truthProperties.getTruthValue())) {
            log.debug("请求被拦截 - URL(未带请求头'truth'): {}, Method: {}", request.getRequestURI(), request.getMethod());
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null);
        }
        // 获取用户uid并存入thread local
        try {
            Long uid = Long.valueOf(request.getHeader(AuthConstants.UID_KEY));
            UserContextUtil.setUid(uid);
        } catch (NumberFormatException e) {
            log.warn("请求被拦截 - URL(未获得uid): {}, Method: {}", request.getRequestURI(), request.getMethod());
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null);
        }

        // 获取用户身份并存入thread local
        String identity = request.getHeader(AuthConstants.IDENTITY_KEY);
        if (identity == null || identity.isEmpty()) {
            log.warn("请求被拦截 - URL(未获得identity): {}, Method: {}", request.getRequestURI(), request.getMethod());
            throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null);
        }
        UserContextUtil.setIdentity(identity);

        // 如果是管理员，获取等级并存入thread local
        if (AuthConstants.IDENTITY_ADMIN_USER_VALUE_.equals(identity)) {
            String adminLevelStr = request.getHeader(AuthConstants.ADMIN_LEVEL_KEY);
            if (adminLevelStr == null || adminLevelStr.isEmpty()) {
                log.warn("请求被拦截 - URL(管理员未获得admin_level): {}, Method: {}", request.getRequestURI(), request.getMethod());
                throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null);
            }
            try {
                Integer adminLevel = Integer.valueOf(adminLevelStr);
                UserContextUtil.setAdminLevel(adminLevel);
            } catch (NumberFormatException e) {
                log.warn("请求被拦截 - URL(admin_level格式错误): {}, Method: {}", request.getRequestURI(), request.getMethod());
                throw new ForYourselfException(ResultCodeEnum.CAPTCHA_VERIFICATION_FAILED, null);
            }
        }

        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextUtil.clear();
    }
}
