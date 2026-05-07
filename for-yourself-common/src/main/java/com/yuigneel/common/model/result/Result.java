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
package com.yuigneel.common.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 全局统一返回结果类
 */
@Data
@Schema(description = "全局响应结果")
public class Result<T> {
    //yu: 定义基础属性
    //返回码
    @Schema(description = "业务响应码", example = "00000")
    private String code;

    //返回消息
    @Schema(description = "响应消息")
    private String message;

    //返回数据
    @Schema(description = "响应数据")
    private T data;

    //lgnier:你不准new
    private Result() {
    }

    //yu_lgnier：你用我的方法构建
    public static <T> Result<T> build() {
        return new Result<>();
    }

    //伊格尼尔：你自由补充

    public static <T> Result<T> buildDIY(String code, String message, T data) {
        Result<T> result = build();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    //yu·lgnier:默认ok选项
    public static <T> Result<T> ok() {
       return buildDIY(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), null);
    }

    //y u l gnier:接收枚举参数
    public static <T> Result<T> ok(ResultCodeEnum resultCodeEnum) {
        return buildDIY(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), null);
    }

    //lgnier~yu:接收数据
    public static <T> Result<T> ok(T data) {
        return buildDIY(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    //lgnier~yu:接收枚举参数和数据
    public static <T> Result<T> ok(ResultCodeEnum resultCodeEnum, T data) {
       return buildDIY(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    //Y u·L g n i e r：默认失败选项
    public static <T> Result<T> fail() {
        return buildDIY(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage(), null);
    }

    //Y U·l g n i e r:接收枚举参数
    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum) {
       return buildDIY(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), null);
    }

    //lgnier~yu:接收数据
    public static <T> Result<T> fail(T data) {
        return buildDIY(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage(), data);
    }

    //lgnier~yu:接收枚举参数和数据
    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum, T data) {
        return buildDIY(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    //lgnier~yu:接收code&message
    public static <T> Result<T> fail(String code, String message, T data) {
       return buildDIY(code, message, data);
    }
}
