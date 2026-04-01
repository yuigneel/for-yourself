package com.yulgnier.common.model.result;

/**
 * 统一返回结果状态信息类
 */
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    PARAM_ERROR(202, "参数不正确"),
    SERVICE_ERROR(203, "服务异常"),
    DATA_ERROR(204, "数据异常"),
    ILLEGAL_REQUEST(205, "非法请求"),
    REPEAT_SUBMIT(206, "重复提交"),
    DELETE_ERROR(207, "请先删除子集"),

    // 通用校验错误（400-499）
    EMAIL_FORMAT_ERROR(401, "邮箱格式不正确"),
    PHONE_FORMAT_ERROR(402, "手机号码格式不正确"),
    ID_CARD_FORMAT_ERROR(403, "身份证号码格式不正确"),
    USERNAME_FORMAT_ERROR(404, "用户名格式不正确"),
    PASSWORD_FORMAT_ERROR(405, "密码格式不正确"),
    PASSWORD_TOO_SIMPLE(406, "密码过于简单"),
    PASSWORD_NOT_MATCH(407, "两次输入的密码不一致"),
    VERIFICATION_CODE_ERROR(408, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(409, "验证码已过期"),
    VERIFICATION_CODE_NOT_FOUND(410, "未输入验证码"),
    REQUEST_TOO_OFTEN(411, "操作过于频繁"),
    DATA_NOT_FOUND(412, "数据不存在"),
    DATA_ALREADY_EXISTS(413, "数据已存在"),
    REQUEST_INCOMPLETE(414, "请求信息不完整"),

    ADMIN_ACCOUNT_EXIST_ERROR(301, "账号已存在"),
    ADMIN_CAPTCHA_CODE_ERROR(302, "验证码错误"),
    ADMIN_CAPTCHA_CODE_EXPIRED(303, "验证码已过期"),
    ADMIN_CAPTCHA_CODE_NOT_FOUND(304, "未输入验证码"),

    ADMIN_LOGIN_AUTH(305, "未登陆"),
    ADMIN_ACCOUNT_NOT_EXIST_ERROR(306, "账号不存在"),
    ADMIN_ACCOUNT_ERROR(307, "用户名或密码错误"),
    ADMIN_ACCOUNT_DISABLED_ERROR(308, "该用户已被禁用"),
    ADMIN_ACCESS_FORBIDDEN(309, "无访问权限"),

    APP_LOGIN_AUTH(501, "未登陆"),
    APP_LOGIN_PHONE_EMPTY(502, "手机号码为空"),
    APP_LOGIN_CODE_EMPTY(503, "验证码为空"),
    APP_SEND_SMS_TOO_OFTEN(504, "验证码发送过于频繁"),
    APP_LOGIN_CODE_EXPIRED(505, "验证码已过期"),
    APP_LOGIN_CODE_ERROR(506, "验证码错误"),
    APP_ACCOUNT_DISABLED_ERROR(507, "该用户已被禁用"),
    APP_PHONE_FORMAT_ERROR(508, "手机号码格式不正确"),
    APP_EMAIL_FORMAT_ERROR(509, "邮箱格式不正确"),


    TOKEN_EXPIRED(601, "token 过期"),
    TOKEN_INVALID(602, "token 非法"),
    TOKEN_GENERATE_ERROR(603, "token 生成失败"),
    PERMISSION_DENIED(604, "权限不足");

    //根据枚举的定义，不能加static，因为枚举是类，不能有静态成员变量
    private final Integer code;

    private final String message;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
