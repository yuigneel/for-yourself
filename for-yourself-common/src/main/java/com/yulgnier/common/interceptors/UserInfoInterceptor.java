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

        // 获取link验证token并存入thread local
        String link = request.getHeader(AuthConstants.LINK_KEY);
        if (link != null && !link.isEmpty()) {
            UserContextUtil.setLink(link);
        }

        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextUtil.clear();
    }
}
