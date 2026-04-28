package com.yuigneel.common.exception;

import com.yuigneel.common.model.result.ResultCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常，继承RuntimeException，用于统一处理业务层异常场景
 * '@EqualsAndHashCode(callSuper = true) 注解核心解释：'
 * 1. 注解作用：控制Lombok生成equals()和hashCode()方法时，是否纳入父类的属性参与计算
 * 2. 必须添加的原因：
 * - 当前异常类通过@Data注解自动生成equals()、hashCode()，Lombok默认仅基于【当前类自定义字段】生成逻辑
 * - 本类继承自RuntimeException，而RuntimeException的父类Throwable包含核心属性：message（异常信息）、cause（异常根源）、stackTrace（堆栈信息）等
 * - 异常的相等性判断，核心依赖父类的message、cause等关键属性，仅用子类字段判断会丢失异常核心特征
 * 3. 不添加的潜在问题：
 * - 相等性判断失效：两个message、cause完全相同的异常实例，equals()会返回false，违背异常相等性的业务认知
 * - 集合使用异常：将异常存入HashMap、HashSet等基于hashCode()和equals()判重的集合时，会出现“重复存储”“查找不到”的问题
 * - 测试/日志校验失败：单元测试中断言异常属性、日志系统中异常去重统计时，会出现逻辑错误，影响问题排查
 * - 违背Java规范：Java中equals()和hashCode()的设计原则要求，子类重写时需保证与父类逻辑兼容，异常类更需遵循此规范
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ForYourselfException extends RuntimeException {
    /**
     * 异常状态码
     * 用于标识具体的业务错误类型
     */
    private String code;
    /**
     * 返回给前端的异常体 Data
     */
    private Object data;

    /**
     * 构造函数 1: 通过状态码和消息直接创建异常

     * @param code 状态码，例如：500 表示服务器错误，400 表示参数错误
     * @param message 异常描述信息，说明具体错误内容

     * 使用场景：当你需要自定义状态码和错误消息时使用
     * 示例：new ForYourselfException(400, "用户输入的邮箱格式不正确")
     */
    public ForYourselfException(String code, String message, Object data) {
        super(message);  // 调用父类 RuntimeException 的构造函数，将消息传递给异常体系
        this.code = code;  // 初始化本类的 code 字段
        this.data = data;
    }

    /**
     * 构造函数 2: 通过枚举结果码创建异常
     * 
     * @param resultCodeEnum ResultCodeEnum 枚举对象，预先定义好的状态码和消息组合

     * 使用场景：当错误是预定义的标准错误时，直接使用枚举更方便
     * 优势：统一管理所有错误码，避免硬编码，便于维护
     * 示例：new ForYourselfException(ResultCodeEnum.PARAM_ERROR)
     */
    public ForYourselfException(ResultCodeEnum resultCodeEnum, Object data) {
        super(resultCodeEnum.getMessage());  // 从枚举中获取错误消息并传递给父类
        this.code = resultCodeEnum.getCode();  // 从枚举中获取状态码并赋值给本类的 code 字段
        this.data = data;
    }
}
