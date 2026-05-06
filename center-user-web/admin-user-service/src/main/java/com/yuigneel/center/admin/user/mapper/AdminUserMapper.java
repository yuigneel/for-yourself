package com.yuigneel.center.admin.user.mapper;

import com.yuigneel.center.admin.user.model.domain.AdminUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员用户 Mapper 接口
 *
 * @author 羽·伊格尼尔
 * @since 2026-05-06
 */
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    /**
     * 根据昵称查询用户（忽略逻辑删除）
     *
     * @param nickname 昵称
     * @return 管理员用户
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUser selectOneByNicknameIgnoreLogicDelete(String nickname);

    /**
     * 恢复已逻辑删除的用户（取消逻辑删除）
     *
     * @param adminUser 管理员用户对象
     * @return 影响行数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    int restoreUserIgnoreLogicDelete(AdminUser adminUser);

    /**
     * 根据邮箱查询用户（忽略逻辑删除）
     *
     * @param email 邮箱
     * @return 管理员用户
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUser selectOneByEmailIgnoreLogicDelete(String email);

    /**
     * 根据UID查询用户（忽略逻辑删除）
     *
     * @param uid 用户UID
     * @return 管理员用户
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    AdminUser selectOneByUidIgnoreLogicDelete(Long uid);

    /**
     * 物理删除单个管理员账号（直接 DELETE，不走逻辑删除）
     *
     * @param uid 需要删除的管理员 UID
     * @return 影响行数
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    int physicalDeleteByUid(@Param("uid") Long uid);

    /**
     * 查询待清理的管理员 UID 列表（忽略逻辑删除）
     *
     * @param logicDeleteThreshold 逻辑删除的时间阈值
     * @param forceLogoutThreshold 强制销号的时间阈值
     * @param batchSize 每批查询的数量
     * @return UID 列表
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    java.util.List<Long> selectUidsForCleanup(@Param("logicThreshold") java.time.LocalDateTime logicDeleteThreshold,
                                              @Param("forceThreshold") java.time.LocalDateTime forceLogoutThreshold,
                                              @Param("limit") int batchSize);

    /**
     * 查询待清理的管理员 UID 列表（忽略逻辑删除，并排除指定 UID）
     *
     * @param logicDeleteThreshold 逻辑删除的时间阈值
     * @param forceLogoutThreshold 强制销号的时间阈值
     * @param batchSize 每批查询的数量
     * @param excludeUids 需要排除的 UID 列表（删除失败的 UID）
     * @return UID 列表
     * @author 羽·伊格尼尔
     * @since 2026-05-06
     */
    java.util.List<Long> selectUidsForCleanupExcluding(@Param("logicThreshold") java.time.LocalDateTime logicDeleteThreshold,
                                                       @Param("forceThreshold") java.time.LocalDateTime forceLogoutThreshold,
                                                       @Param("limit") int batchSize,
                                                       @Param("excludeUids") java.util.List<Long> excludeUids);
}




