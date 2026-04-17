package com.yulgnier.common.exception;


import com.yulgnier.common.model.result.Result;
import com.yulgnier.common.model.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice   // 全局异常处理,让这个类能捕获所有controller抛出的异常
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)  // 指定要处理的异常类型
    @ResponseBody   // 返回json数据, 而不是跳转页面
    public Result handle(Exception e) {
        log.error("[默认全局异常处理]", e);   // 打印异常信息
        return Result.fail();
    }

    @ExceptionHandler(ForYourselfException.class)
    @ResponseBody
    public Result handle(ForYourselfException e) {
        log.debug("[自定义全局异常处理]", e);
        return Result.fail(e.getCode(), e.getMessage(), e.getData());
    }
    /**
     * 捕获@NotBlank注解抛出的异常，封装成自定义异常并抛出
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result handle(MethodArgumentNotValidException e) {
        log.debug("[参数空值异常处理]",e);
        return Result.fail(ResultCodeEnum.INCOMPLETE_PARAMETERS.getCode(), ResultCodeEnum.INCOMPLETE_PARAMETERS.getMessage(), null);
    }
}
