package com.yuigneel.center.common.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuigneel.center.common.user.model.domain.AccountExceptionStatusTime;

/**
 * @author Yu_Lgnier
 * @description 针对表【t_account_exception_status_time(账号异常状态时间表)】的数据库操作Mapper
 * @createDate 2026-05-02
 */
public interface AccountExceptionStatusTimeMapper extends BaseMapper<AccountExceptionStatusTime> {

    /**
     * 物理删除异常状态记录（不经过逻辑删除）
     * @param uid 用户ID
     * @param identityType 身份类型
     */
    int physicalDeleteByUidAndIdentity(@org.apache.ibatis.annotations.Param("uid") Long uid, 
                                       @org.apache.ibatis.annotations.Param("identityType") com.yuigneel.common.model.enums.AccountIdentityTypeEnum identityType);
}
