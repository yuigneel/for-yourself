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
package com.yuigneel.center.user.api.client;

import com.yuigneel.common.config.DefaultFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * 普通用户服务 Feign 客户端
 *
 * @author yulgnier
 * @since 2026-04-21
 */
@FeignClient(name = "center-common-user-service", contextId = "CommonUserClient", path = "/center-common/user", configuration = DefaultFeignClientConfig.class)
public interface CommonUserClient {

}
