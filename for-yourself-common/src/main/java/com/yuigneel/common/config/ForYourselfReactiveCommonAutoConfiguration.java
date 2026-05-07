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
package com.yuigneel.common.config;
import com.yuigneel.common.config.properties.GateWayProperties;
import com.yuigneel.common.config.properties.JwtProperties;
import com.yuigneel.common.config.properties.SnowflakeProperties;
import com.yuigneel.common.config.properties.TruthProperties;
import com.yuigneel.common.utils.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 【仅 WebFlux / 网关 项目生效】
 * type = REACTIVE ：适配 Spring Cloud Gateway 环境
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
// 导入 网关需要的所有通用类（✅ 无任何 MVC 依赖，完美兼容）
@Import({
        RedisConfig.class, JwtProperties.class, SnowflakeProperties.class,
        JacksonConfig.class, SecurityConfig.class,SnowflakeConfig.class,
        JwtUtil.class, TruthProperties.class,
        GateWayProperties.class
})
public class ForYourselfReactiveCommonAutoConfiguration {

}
