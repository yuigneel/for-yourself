package com.yuigneel.common.exception;


import com.yuigneel.common.model.result.Result;
import com.yuigneel.common.model.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice   // 全局异常处理,让这个类能捕获所有controller抛出的异常
public class GlobalExceptionHandler {
    @ResponseBody   // 返回json数据, 而不是跳转页面
    @ExceptionHandler(Exception.class)  // 指定要处理的异常类型
    public Result handle(Exception e) {
        log.error("[默认全局异常处理]", e);   // 打印异常信息
        return Result.fail();
    }

    @ResponseBody
    @ExceptionHandler(ForYourselfException.class)
    public Result handle(ForYourselfException e) {
        log.debug("[自定义全局异常处理]", e);
        return Result.fail(e.getCode(), e.getMessage(), e.getData());
    }
    /**
     * 捕获@NotBlank注解抛出的异常,封装成自定义异常并抛出
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handle(MethodArgumentNotValidException e) {
        log.debug("[参数空值异常处理]",e);
        // 获取第一个字段错误的默认消息
         /*步骤解析:
             1. e.getBindingResult() - 从异常中获取绑定结果对象,包含所有校验失败的信息
             2. .getFieldErrors() - 获取所有字段校验错误的列表(List<FieldError>)
             3. .stream() - 将List转换为Stream流,便于进行函数式操作
             4. .findFirst() - 获取第一个错误(返回Optional<FieldError>,可能为空)
             5. .map(fieldError -> fieldError.getDefaultMessage()) - 如果有错误,提取其默认消息(即@NotBlank等注解中message属性的值)
             6. .orElse("参数校验失败") - 如果前面没有获取到错误( Optional为空),则使用默认提示文本
          */
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("参数校验失败");
        return Result.fail(ResultCodeEnum.INCOMPLETE_PARAMETERS.getCode(), errorMessage, null);
    }
}
