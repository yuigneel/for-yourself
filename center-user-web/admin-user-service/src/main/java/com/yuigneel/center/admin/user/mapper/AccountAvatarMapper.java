package com.yuigneel.center.admin.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuigneel.center.admin.user.model.domain.AccountAvatar;
import org.apache.ibatis.annotations.Param;


/**
 * 账号头像 Mapper 接口
 *
 * @author yulgnier
 * @date 2025
 */
public interface AccountAvatarMapper extends BaseMapper<AccountAvatar> {

    /**
     * 根据UID和身份类型物理删除（忽略逻辑删除）
     *
     * @param uid          用户/管理员唯一业务UID
     * @param identityType 身份类型
     * @return 影响行数
     */
    int deleteByUidAndIdentityTypeIgnoreLogic(@Param("uid") Long uid, @Param("identityType") Integer identityType);
}
