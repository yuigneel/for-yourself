package com.yulgnier.common.model.result;

/**
 * 统一返回结果状态信息类
 */
public enum ResultCodeEnum {

    // ====================== 200 通用基础 ======================
    SUCCESS(200, "操作成功"),
    FAIL(201, "操作失败"),

    // ====================== 300 管理后台 Admin 专属 ======================
    ADMIN_LOGIN_REQUIRED(300, "请先登录"),
    ADMIN_ACCOUNT_EXIST(301, "账号已存在"),
    ADMIN_ACCOUNT_NOT_EXIST(302, "账号不存在"),
    ADMIN_ACCOUNT_DISABLED(303, "账号已被禁用"),
    ADMIN_ACCOUNT_PASSWORD_ERROR(304, "用户名或密码错误"),
    ADMIN_PERMISSION_DENIED(305, "无访问权限"),

    // ====================== 400 客户端通用 - 参数/格式校验 ======================
    PARAM_ERROR(400, "参数不正确"),
    REQUEST_INCOMPLETE(401, "请求信息不完整"),
    ILLEGAL_REQUEST(402, "非法请求"),
    REPEAT_SUBMIT(403, "重复提交"),

    // 格式校验
    EMAIL_FORMAT_ERROR(405, "邮箱格式不正确"),
    PHONE_FORMAT_ERROR(406, "手机号码格式不正确"),
    ID_CARD_FORMAT_ERROR(407, "身份证格式不正确"),
    USERNAME_FORMAT_ERROR(408, "用户名格式不正确"),
    PASSWORD_FORMAT_ERROR(409, "密码格式不正确"),
    PASSWORD_TOO_SIMPLE(410, "密码过于简单"),
    PASSWORD_NOT_MATCH(411, "两次密码不一致"),
    DATE_FORMAT_ERROR(412, "日期格式不正确"),

    // ====================== 420 客户端通用 - 数据操作 ======================
    DATA_NOT_FOUND(420, "数据不存在"),
    DATA_ALREADY_EXISTS(421, "数据已存在"),
    DELETE_HAS_CHILDREN(422, "请先删除子数据"),
    USER_ALREADY_EXISTS(423, "用户已存在"),

    // ====================== 440 核心补充 - 验证码/发送风控（你最需要的） ======================
    VERIFICATION_CODE_NOT_INPUT(440, "未输入验证码"),
    VERIFICATION_CODE_ERROR(441, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(442, "验证码已过期"),
    VERIFICATION_CODE_USED(443, "验证码已使用"),
    VERIFICATION_CODE_NOT_SEND(444, "验证码未发送"),

    // 发送限制/风控
    CODE_SEND_TOO_OFTEN(450, "操作频繁，请稍后再试"),
    EMAIL_CODE_SEND_LIMIT(451, "邮箱验证码已发送，请勿重复发送"),
    EMAIL_SEND_COUNT_OVER(452, "今日发送次数已达上限"),
    EMAIL_BANNED_SEND(453, "该邮箱已被禁止发送验证码"),
    IP_BANNED(454, "当前IP请求异常，已临时限制"),

    // ====================== 500 APP 端专属 ======================
    APP_LOGIN_REQUIRED(500, "请先登录"),
    APP_PHONE_EMPTY(501, "手机号码不能为空"),
    APP_CODE_EMPTY(502, "验证码不能为空"),
    APP_ACCOUNT_DISABLED(503, "账号已被禁用"),
    APP_SMS_TOO_OFTEN(504, "短信发送过于频繁"),

    // ====================== 600 Token/权限/安全 ======================
    TOKEN_INVALID(600, "token 非法"),
    TOKEN_EXPIRED(601, "token 已过期"),
    TOKEN_GENERATE_ERROR(602, "token 生成失败"),
    PERMISSION_DENIED(603, "权限不足"),
    ACCESS_FORBIDDEN(604, "禁止访问"),

    // ====================== 700 系统/服务异常 ======================
    SERVICE_ERROR(700, "服务异常，请稍后重试"),
    DATA_ERROR(701, "数据异常"),
    SYSTEM_BUSY(702, "系统繁忙"),

    // ====================== 800 功能开发状态 ======================
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
