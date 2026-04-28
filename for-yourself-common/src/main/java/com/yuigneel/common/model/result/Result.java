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
