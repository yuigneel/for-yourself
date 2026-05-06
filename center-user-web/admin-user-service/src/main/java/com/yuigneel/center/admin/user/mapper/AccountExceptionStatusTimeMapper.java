package com.yuigneel.center.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuigneel.center.admin.user.model.domain.AccountExceptionStatusTime;

/**
 * 账号异常状态时间 Mapper 接口
 *
 * @author Yu·Igneel
 * @since 2026-05-06
 */
public interface AccountExceptionStatusTimeMapper extends BaseMapper<AccountExceptionStatusTime> {

    /**
     * 物理删除异常状态记录（不经过逻辑删除）
     *
     * @param uid          用户ID
     * @param identityType 身份类型
     * @return 影响行数
     * @author Yu·Igneel
     * @since 2026-05-06
     */
    int physicalDeleteByUidAndIdentity(@org.apache.ibatis.annotations.Param("uid") Long uid, 
                                       @org.apache.ibatis.annotations.Param("identityType") com.yuigneel.common.model.enums.AccountIdentityTypeEnum identityType);
}
