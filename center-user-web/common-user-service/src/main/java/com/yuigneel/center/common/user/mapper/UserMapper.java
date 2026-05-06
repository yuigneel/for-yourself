package com.yuigneel.center.common.user.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuigneel.center.common.user.model.domain.CommonUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuigneel.center.user.api.model.dto.CommonUserPageQueryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 普通用户 Mapper 接口
 *
 * @author 逆羽风辰
 * @since 2026-05-06
 */
public interface UserMapper extends BaseMapper<CommonUser> {

    /**
     * 根据昵称查询用户(忽略逻辑删除)
     *
     * @param nickname 用户昵称
     * @return 用户对象
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    CommonUser selectOneByNicknameIgnoreLogicDelete(String nickname);

    /**
     * 根据邮箱查询用户(忽略逻辑删除)
     *
     * @param email 用户邮箱
     * @return 用户对象
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    CommonUser selectOneByEmailIgnoreLogicDelete(String email);

    /**
     * 根据UID查询用户(忽略逻辑删除)
     *
     * @param uid 用户 UID
     * @return 用户对象
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    CommonUser selectOneByUidIgnoreLogicDelete(Long uid);

    /**
     * 根据UID修改用户密码(忽略逻辑删除)
     *
     * @param uid      用户 UID
     * @param password 加密后的新密码
     * @return 影响行数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    int updatePasswordByUidIgnoreLogicDelete(Long uid, String password);

    /**
     * 恢复用户-将is_deleted设为0(忽略逻辑删除)
     *
     * @param user 用户对象(需包含uid)
     * @return 影响行数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    int restoreUserIgnoreLogicDelete(CommonUser user);

    /**
     * 分页查询普通用户列表（支持动态条件）
     *
     * @param page  分页对象（MyBatis-Plus 自动注入 LIMIT/OFFSET）
     * @param query 查询条件
     * @return 分页结果
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    IPage<CommonUser> selectPageByCondition(Page<CommonUser> page, @Param("query") CommonUserPageQueryDTO query);

    /**
     * 物理删除单个普通用户账号（直接 DELETE，不走逻辑删除）
     *
     * @param uid 需要删除的用户 UID
     * @return 影响行数
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    int physicalDeleteByUid(@Param("uid") Long uid);

    /**
     * 查询待清理的 UID 列表（忽略逻辑删除）
     *
     * @param logicDeleteThreshold 逻辑删除的时间阈值
     * @param forceLogoutThreshold 强制销号的时间阈值
     * @param batchSize            每批查询的数量
     * @return UID 列表
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    List<Long> selectUidsForCleanup(@Param("logicThreshold") java.time.LocalDateTime logicDeleteThreshold,
                                    @Param("forceThreshold") java.time.LocalDateTime forceLogoutThreshold,
                                    @Param("limit") int batchSize);

    /**
     * 查询待清理的 UID 列表（忽略逻辑删除，并排除指定 UID）
     *
     * @param logicDeleteThreshold 逻辑删除的时间阈值
     * @param forceLogoutThreshold 强制销号的时间阈值
     * @param batchSize            每批查询的数量
     * @param excludeUids          需要排除的 UID 列表（删除失败的 UID）
     * @return UID 列表
     * @author 逆羽风辰
     * @since 2026-05-06
     */
    List<Long> selectUidsForCleanupExcluding(@Param("logicThreshold") java.time.LocalDateTime logicDeleteThreshold,
                                             @Param("forceThreshold") java.time.LocalDateTime forceLogoutThreshold,
                                             @Param("limit") int batchSize,
                                             @Param("excludeUids") java.util.List<Long> excludeUids);

}




