package com.yulgnier.common.model.result;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 统一返回结果状态信息类
 * <p>
 * 错误码采用三级分层结构（类比：省份-城市-门牌号）：
 * <ul>
 *   <li><b>一级错误码</b>（首字母）：宏观总分类，快速定责
 *     <ul>
 *       <li>A - 用户端错误（参数错误、未登录、权限不足等）→ HTTP 4xx</li>
 *       <li>B - 系统内部错误（数据库异常、代码报错等）→ HTTP 5xx</li>
 *       <li>C - 第三方服务错误（支付超时、短信服务挂掉等）→ HTTP 502/504</li>
 *     </ul>
 *   </li>
 *   <li><b>二级错误码</b>（第2-3位数字）：中观模块分类，定位业务域
 *     <ul>
 *       <li>示例：A01xx = 用户注册模块，A02xx = 用户登录模块，B02xx = 数据库模块</li>
 *       <li>用途：日志分类统计、全局异常处理</li>
 *     </ul>
 *   </li>
 *   <li><b>三级错误码</b>（第4-5位数字）：微观场景分类，精准定位具体问题
 *     <ul>
 *       <li>示例：A0101 = 用户未同意隐私协议，A0111 = 用户名已存在</li>
 *       <li>用途：前端精准提示、问题快速排查、国际化文案映射</li>
 *     </ul>
 *   </li>
 * </ul>
 * </p>
 */
public enum ResultCodeEnum {

    // ====================== 占位模糊响应 ======================
    SUCCESS("00000", "成功"),
    FAIL("11111", "失败"),
    TODO("22333","功能未实现"),
    // ====================== Axxxx 用户端错误 ======================
    USER_SIDE_ERROR("A0001", "用户端错误"),
    // A01xx 访问系统错误
    CAPTCHA_VERIFICATION_FAILED("A0100", "人机验证失败"),
    CAPTCHA_REQUEST_TOO_FREQUENT("A0101", "验证码请求过于频繁"),
    CAPTCHA_ALREADY_SENT("A0102", "验证码已发送"),
    ILLEGAL_ACCESS("A0103", "非法访问"),
    // A02xx 请求参数错误
    INCOMPLETE_PARAMETERS("A0200", "请求参数不完整"),
    PARAMETER_ERROR("A0201", "请求参数错误"),
    USERNAME_FORMAT_ERROR("A0202", "用户名格式错误"),
    EMAIL_FORMAT_ERROR("A0203", "邮箱格式错误"),
    PASSWORD_FORMAT_ERROR("A0205", "密码格式错误"),
    PHONE_FORMAT_ERROR("A0206", "手机号格式错误"),
    DATE_FORMAT_ERROR("A0207", "日期格式错误"),

    USERNAME_ALREADY_EXISTS("A0210", "用户名已存在"),
    PHONE_ALREADY_EXISTS("A0211", "手机号已存在"),
    EMAIL_ALREADY_EXISTS("A0212", "邮箱已存在"),
    UID_ALREADY_EXISTS("A0213", "UID已存在"),

    USERNAME_NOT_FOUND("A0220", "用户名不存在"),
    PHONE_NOT_FOUND("A0221", "手机号不存在"),
    EMAIL_NOT_FOUND("A0222", "邮箱不存在"),
    UID_NOT_FOUND("A0223", "UID不存在"),
    // A03xx 鉴权错误
    CAPTCHA_ERROR("A0300", "验证码错误"),
    PASSWORD_ERROR("A0301", "密码错误"),
    CAPTCHA_EXPIRED("A0302", "验证码已过期"),
    ACCOUNT_OR_PASSWORD_ERROR("A0303", "账户或密码错误"),
    ACCOUNT_BANNED("A0304", "账户已被封禁"),
    ACCOUNT_CANCELLED("A0305", "账户已注销"),
    USER_NOT_FOUND_OR_CANCELLED("A0306", "用户不存在或已注销"),
    INSUFFICIENT_PERMISSIONS("A0307", "权限不足"),
    // A04xx 用户隐私
    // A05xx 用户资产
    SAVE_USER_FAILED("A0500", "保存用户失败"),
    USER_NOT_FOUND("A0501", "用户不存在"),
    // A06xx 传输错误
    FEIGN_CLIENT_CALL_ERROR("A0600", "FeignClient调用异常"),
    REMOTE_RESPONSE_ERROR("A0601", "远程返回结果错误"),
    // A07xx 用户设备错误
    // A08xx 用户状态
    USER_NORMAL_LOGIN("A0800", "用户正常登录"),
    USER_CANCELLED_UNDO_LOGIN("A0801", "用户取消注销并登录"),

    // ====================== B0xxx 系统执行出错 ======================
    SYSTEM_EXECUTION_ERROR("B0001", "系统执行出错"),

    // B01xx 系统执行超时
    SYSTEM_EXECUTION_TIMEOUT("B0100", "系统执行超时"),
    ORDER_PROCESSING_TIMEOUT("B0101", "系统订单处理超时"),

    // B02xx 系统容灾功能被触发
    DISASTER_RECOVERY_TRIGGERED("B0200", "系统容灾功能被触发"),
    SYSTEM_RATE_LIMITED("B0210", "系统限流"),
    SYSTEM_FEATURE_DOWNGRADED("B0220", "系统功能降级"),

    // B03xx 系统资源异常
    SYSTEM_RESOURCE_ERROR("B0300", "系统资源异常"),
    SYSTEM_RESOURCE_EXHAUSTED("B0310", "系统资源耗尽"),
    DISK_SPACE_EXHAUSTED("B0311", "系统磁盘空间耗尽"),
    MEMORY_EXHAUSTED("B0312", "系统内存耗尽"),
    FILE_HANDLE_EXHAUSTED("B0313", "文件句柄耗尽"),
    CONNECTION_POOL_EXHAUSTED("B0314", "系统连接池耗尽"),
    THREAD_POOL_EXHAUSTED("B0315", "系统线程池耗尽"),

    SYSTEM_RESOURCE_ACCESS_ERROR("B0320", "系统资源访问异常"),
    READ_DISK_FILE_FAILED("B0321", "系统读取磁盘文件失败"),

    // ====================== C0xxx 调用第三方服务出错 ======================
    THIRD_PARTY_SERVICE_ERROR("C0001", "调用第三方服务出错"),

    // C01xx 中间件服务出错
    MIDDLEWARE_SERVICE_ERROR("C0100", "中间件服务出错"),
    RPC_SERVICE_ERROR("C0110", "RPC 服务出错"),
    RPC_SERVICE_NOT_FOUND("C0111", "RPC 服务未找到"),
    RPC_SERVICE_NOT_REGISTERED("C0112", "RPC 服务未注册"),
    INTERFACE_NOT_EXIST("C0113", "接口不存在"),

    MESSAGE_SERVICE_ERROR("C0120", "消息服务出错"),
    MESSAGE_DELIVERY_ERROR("C0121", "消息投递出错"),
    MESSAGE_CONSUMPTION_ERROR("C0122", "消息消费出错"),
    MESSAGE_SUBSCRIPTION_ERROR("C0123", "消息订阅出错"),
    MESSAGE_GROUP_NOT_FOUND("C0124", "消息分组未查到"),

    CACHE_SERVICE_ERROR("C0130", "缓存服务出错"),
    CACHE_KEY_LENGTH_EXCEEDS_LIMIT("C0131", "key 长度超过限制"),
    CACHE_VALUE_LENGTH_EXCEEDS_LIMIT("C0132", "value 长度超过限制"),
    CACHE_STORAGE_FULL("C0133", "存储容量已满"),
    UNSUPPORTED_DATA_FORMAT("C0134", "不支持的数据格式"),

    CONFIG_SERVICE_ERROR("C0140", "配置服务出错"),

    NETWORK_RESOURCE_SERVICE_ERROR("C0150", "网络资源服务出错"),
    VPN_SERVICE_ERROR("C0151", "VPN 服务出错"),
    CDN_SERVICE_ERROR("C0152", "CDN 服务出错"),
    DOMAIN_RESOLUTION_SERVICE_ERROR("C0153", "域名解析服务出错"),
    GATEWAY_SERVICE_ERROR("C0154", "网关服务出错"),

    // C02xx 第三方系统执行超时
    THIRD_PARTY_EXECUTION_TIMEOUT("C0200", "第三方系统执行超时"),
    RPC_EXECUTION_TIMEOUT("C0210", "RPC 执行超时"),
    MESSAGE_DELIVERY_TIMEOUT("C0220", "消息投递超时"),
    CACHE_SERVICE_TIMEOUT("C0230", "缓存服务超时"),
    CONFIG_SERVICE_TIMEOUT("C0240", "配置服务超时"),
    DATABASE_SERVICE_TIMEOUT("C0250", "数据库服务超时"),

    // C03xx 数据库服务出错
    DATABASE_SERVICE_ERROR("C0300", "数据库服务出错"),
    TABLE_NOT_EXIST("C0311", "表不存在"),
    COLUMN_NOT_EXIST("C0312", "列不存在"),
    MULTIPLE_TABLES_SAME_COLUMN_NAME("C0321", "多表关联中存在多个相同名称的列"),
    DATABASE_DEADLOCK("C0331", "数据库死锁"),
    PRIMARY_KEY_CONFLICT("C0341", "主键冲突"),

    // C04xx 第三方容灾系统被触发
    THIRD_PARTY_DISASTER_RECOVERY_TRIGGERED("C0400", "第三方容灾系统被触发"),
    THIRD_PARTY_RATE_LIMITED("C0401", "第三方系统限流"),
    THIRD_PARTY_FEATURE_DOWNGRADED("C0402", "第三方功能降级"),

    // C05xx 通知服务出错
    NOTIFICATION_SERVICE_ERROR("C0500", "通知服务出错"),
    SMS_NOTIFICATION_FAILED("C0501", "短信提醒服务失败"),
    VOICE_NOTIFICATION_FAILED("C0502", "语音提醒服务失败"),
    EMAIL_NOTIFICATION_FAILED("C0503", "邮件提醒服务失败");

    private final String code;

    private final String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
