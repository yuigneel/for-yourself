/*
 * For Yourself - A graduation project by the author, serving as a demonstration for the future complete project ecosystem
 * Copyright (C) 2026  Yu·Igneel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.yuigneel.common.interceptors;

import com.yuigneel.common.config.properties.TruthProperties;
import com.yuigneel.common.exception.ForYourselfException;
import com.yuigneel.common.model.constants.AuthConstants;
import com.yuigneel.common.model.result.ResultCodeEnum;
import com.yuigneel.common.utils.UserContextUtil;
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
