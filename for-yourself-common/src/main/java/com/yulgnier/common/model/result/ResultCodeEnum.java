package com.yulgnier.common.model.result;

/**
 * 统一返回结果状态信息类
 */
public enum ResultCodeEnum {

    // ====================== 2xx 操作结果（通用标准） ======================
    SUCCESS(200, "操作成功"),
    FAIL(201, "操作失败"),

    // ====================== 400-409 基础请求错误 ======================
    PARAM_ERROR(400, "请求参数错误"),
    REQUEST_INCOMPLETE(401, "请求信息不完整"),
    ILLEGAL_REQUEST(402, "非法请求"),
    REPEAT_SUBMIT(403, "请勿重复提交"),

    // ====================== 410-419 格式校验错误 ======================
    USERNAME_FORMAT_ERROR(410, "用户名格式不正确"),
    PASSWORD_FORMAT_ERROR(411, "密码格式不正确"),
    PASSWORD_TOO_SIMPLE(412, "密码过于简单"),
    PASSWORD_NOT_MATCH(413, "两次输入密码不一致"),
    PHONE_FORMAT_ERROR(414, "手机号码格式不正确"),
    EMAIL_FORMAT_ERROR(415, "邮箱格式不正确"),
    ID_CARD_FORMAT_ERROR(416, "身份证号码格式不正确"),
    DATE_FORMAT_ERROR(417, "日期格式不正确"),

    // ====================== 420-429 数据操作错误 ======================
    DATA_NOT_FOUND(420, "数据不存在"),
    DATA_ALREADY_EXISTS(421, "数据已存在"),
    DELETE_HAS_CHILDREN(422, "存在子数据，无法删除"),
    USER_ALREADY_EXISTS(423, "用户已存在"),

    // ====================== 430-439 账号状态错误 ======================
    ACCOUNT_EXIST(430, "账号已存在"),
    ACCOUNT_NOT_EXIST(431, "账号不存在"),
    ACCOUNT_DISABLED(432, "账号已被禁用"),
    ACCOUNT_PASSWORD_ERROR(433, "用户名或密码错误"),
    EMAIL_ALREADY_REGISTERED(434, "邮箱已注册"),
    USERNAME_EMAIL_NOT_MATCH(435, "用户名和邮箱不匹配"),

    // ====================== 440-459 验证码 & 风控限流 ======================
    VERIFICATION_CODE_EMPTY(440, "验证码不能为空"),
    VERIFICATION_CODE_ERROR(441, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(442, "验证码已过期"),
    VERIFICATION_CODE_USED(443, "验证码已使用"),
    VERIFICATION_CODE_NOT_SEND(444, "验证码未发送"),

    OPERATION_TOO_FREQUENT(450, "操作频繁，请稍后重试"),
    EMAIL_CODE_SEND_LIMIT(451, "邮箱验证码已发送，请勿重复操作"),
    EMAIL_SEND_COUNT_OVER(452, "邮箱验证码今日发送次数已达上限"),
    EMAIL_SEND_FORBIDDEN(453, "该邮箱已被禁止发送验证码"),
    IP_ACCESS_LIMITED(454, "当前IP请求异常，已临时限制"),
    SMS_SEND_TOO_FREQUENT(455, "短信发送过于频繁"),
    SMS_CODE_EMPTY(456, "短信验证码不能为空"),

    // ====================== 460-469 登录会话 ======================
    LOGIN_REQUIRED(460, "请先登录"),

    // ====================== 600-609 Token & 权限安全 ======================
    TOKEN_INVALID(600, "Token非法"),
    TOKEN_EXPIRED(601, "Token已过期"),
    TOKEN_GENERATE_ERROR(602, "Token生成失败"),
    PERMISSION_DENIED(603, "权限不足，无法访问"),
    ACCESS_FORBIDDEN(604, "禁止访问"),

    // ====================== 700-709 系统/服务异常 ======================
    SERVICE_ERROR(700, "服务异常，请稍后重试"),
    DATA_ERROR(701, "数据异常"),
    SYSTEM_BUSY(702, "系统繁忙，请稍后重试"),

    // ====================== 800-809 功能开发状态 ======================
    FEATURE_NOT_IMPLEMENTED(800, "功能尚未实现");

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
