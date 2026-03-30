package com.yulgnier.common.exception;


import com.yulgnier.common.model.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice   // 全局异常处理,让这个类能捕获所有controller抛出的异常
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)  // 指定要处理的异常类型
    @ResponseBody   // 返回json数据, 而不是跳转页面
    public Result handle(Exception e) {
        e.printStackTrace();    // 打印异常信息
        return Result.fail();
    }
    @ExceptionHandler(ForYourselfException.class)
    @ResponseBody
    public Result handle(ForYourselfException e) {
        e.printStackTrace();
        return Result.fail(e.getCode(), e.getMessage());
    }
}
