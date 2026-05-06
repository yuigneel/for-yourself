package com.yuigneel.common.events;

import com.yuigneel.common.model.enums.AccountIdentityTypeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 【教学注释】用户账号状态校验事件
 * 
 * 1. 为什么要继承 ApplicationEvent？
 *    - Spring 的事件驱动模型要求所有事件都必须继承自 ApplicationEvent。
 *    - 这样 Spring 容器才能识别它是一个“事件”，并分发给对应的监听器。
 * 
 * 2. 为什么要有 source 参数？
 *    - ApplicationEvent 的构造函数要求传入一个 source 对象，通常代表事件的发起者。
 *    - 在这里我们传入 this，表示事件是由当前这个对象触发的。
 */
@Getter
public class UserStatusCheckEvent extends ApplicationEvent {

    /**
     * 需要校验的用户 UID
     */
    private final Long uid;

    /**
     * 用户身份类型（0-管理员，1-普通用户）
     * 用于区分去查哪张表或哪个模块的逻辑
     */
    private final AccountIdentityTypeEnum identityType;

    /**
     * 【核心设计】校验结果回填字段
     * 
     * 1. 初始状态为 null。
     * 2. 拦截器发布事件后，会同步阻塞等待。
     * 3. 监听器（在用户/管理员模块中）收到事件，查库后将结果填入此字段。
     * 4. 拦截器拿到结果，判断是否放行。
     */
    private Boolean isBanned = null;

    /**
     * 封禁到期时间（如果被封禁，记录具体时间方便前端展示）
     */
    private java.time.LocalDateTime expireTime;

    /**
     * 封禁/异常理由
     */
    private String reason;

    /**
     * 构造函数
     * @param source 事件源（通常为 this）
     * @param uid 待校验的用户 ID
     * @param identityType 用户身份
     */
    public UserStatusCheckEvent(Object source, Long uid, AccountIdentityTypeEnum identityType) {
        super(source);
        this.uid = uid;
        this.identityType = identityType;
    }

    /**
     * 【教学注释】供监听器调用的结果设置方法
     * 监听器查完数据库后，调用此方法将结果“回传”给拦截器
     */
    public void setBanned(boolean isBanned, java.time.LocalDateTime expireTime, String reason) {
        this.isBanned = isBanned;
        this.expireTime = expireTime;
        this.reason = reason;
    }
}
