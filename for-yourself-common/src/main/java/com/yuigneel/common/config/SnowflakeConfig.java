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

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.yuigneel.common.config.properties.SnowflakeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 配置类标记
public class SnowflakeConfig {

    // 注入配置
    private final SnowflakeProperties properties;

    // 构造器注入（比@Autowired更规范）
    public SnowflakeConfig(SnowflakeProperties properties) {
        this.properties = properties;
    }

    // 创建雪花算法Bean，全局单例
    @Bean
    public Snowflake snowflake() {
        return IdUtil.createSnowflake(
                properties.getWorkerId(),
                properties.getDataCenterId()
        );
    }
}
