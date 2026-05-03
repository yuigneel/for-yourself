package com.yuigneel.center.common.user.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuigneel.center.common.user.model.domain.CommonUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import org.apache.ibatis.annotations.Param;

/**
* @author Yu_Lgnier
* @description 针对表【t_common_user(普通用户基础信息表)】的数据库操作Mapper
* @createDate 2026-03-30 13:04:36
* @Entity domain.model.com.yuigneel.center.common.user.CommonUser
*/
public interface UserMapper extends BaseMapper<CommonUser> {

    /**
     * 根据昵称查询用户(忽略逻辑删除)
     * @param nickname 用户昵称
     * @return 用户对象
     */
    CommonUser selectOneByNicknameIgnoreLogicDelete(String nickname);

    /**
     * 根据邮箱查询用户(忽略逻辑删除)
     * @param email 用户邮箱
     * @return 用户对象
     */
    CommonUser selectOneByEmailIgnoreLogicDelete(String email);

    /**
     * 根据UID查询用户(忽略逻辑删除)
     * @param uid 用户 UID
     * @return 用户对象
     */
    CommonUser selectOneByUidIgnoreLogicDelete(Long uid);

    /**
     * 根据UID修改用户密码(忽略逻辑删除)
     * @param uid 用户 UID
     * @param password 加密后的新密码
     * @return 影响行数
     */
    int updatePasswordByUidIgnoreLogicDelete(Long uid, String password);

    /**
     * 恢复用户-将is_deleted设为0(忽略逻辑删除)
     * @param user 用户对象(需包含uid)
     * @return 影响行数
     */
    int restoreUserIgnoreLogicDelete(CommonUser user);

    /**
     * 分页查询普通用户列表（支持动态条件）
     * <p>通过 XML 手写 SQL 实现复杂查询逻辑</p>
     *
     * @param page  分页对象（MyBatis-Plus 自动注入 LIMIT/OFFSET）
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<CommonUser> selectPageByCondition(Page<CommonUser> page, @Param("query") CommonUserPageQueryDTO query);

    /**
     * 物理删除用户账号（直接 DELETE，不走逻辑删除）
     * @param uids 需要删除的用户 UID 列表
     * @return 影响行数
     */
    int physicalDeleteByUids(@Param("uids") java.util.List<Long> uids);

}




