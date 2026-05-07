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

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 分页插件配置
 * <p>
 * 作用：
 * 1. 开启 MyBatis-Plus 自动分页能力
 * 2. 拦截所有查询请求，自动拼接 LIMIT 分页语句
 * 3. 支持自带 BaseMapper 分页 + 自定义 XML Mapper 分页
 * <p>
 * 注意：
 * 只要配置这个类，**所有查询都能自动分页**，不需要手动写 limit/offset
 */
@Configuration
public class MyBatisPlusPageConfig {

    /**
     * 注册 MyBatis-Plus 核心插件拦截器
     * 所有 MP 内置插件都通过这个拦截器统一管理
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ------------------------------
        // 分页插件（必须配置才能分页）
        // ------------------------------
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();

        // 设置数据库类型（这里是 MySQL，根据实际数据库修改）
        paginationInterceptor.setDbType(DbType.MYSQL);

        // 开启【溢出总页数后自动跳转到第一页】
        // 比如用户请求第 100 页，但实际只有 10 页，会自动返回第 1 页
        paginationInterceptor.setOverflow(true);

        // 设置单页最大限制条数（防止恶意请求一次性查全表）
        // -1 表示不限制，建议根据业务设置合理值，如 500、1000
        paginationInterceptor.setMaxLimit(500L);

        // 将分页插件加入拦截器链
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
