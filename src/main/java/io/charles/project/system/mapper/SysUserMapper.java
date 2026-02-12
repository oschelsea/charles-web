package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.charles.project.system.domain.SysDept;
import io.charles.project.system.domain.SysRole;
import io.charles.project.system.domain.SysUser;
import io.charles.project.system.domain.SysUserRole;

import java.util.Arrays;
import java.util.List;

/**
 * 用户表 数据层
 *
 * @author charles
 */
public interface SysUserMapper extends MPJBaseMapper<SysUser> {
    /**
     * 根据条件分页查询用户列表
     *
     * @param page 分页对象
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    default List<SysUser> selectUserList(IPage<SysUser> page, SysUser user) {
        MPJLambdaWrapper<SysUser> wrapper = JoinWrappers.lambda();
        wrapper.selectAll(SysUser.class)
                .setAlias("u")
                .selectAssociation(SysDept.class, SysUser::getDept, b -> b.result(SysDept::getDeptName).result(SysDept::getLeader))
                .leftJoin(SysDept.class, "d", SysDept::getDeptId, SysUser::getDeptId);
        wrapper.eq(SysUser::getDelFlag, "0")
                .eqIfExists(SysUser::getUserId, user.getUserId())
                .likeIfExists(SysUser::getUserName, user.getUserName())
                .eqIfExists(SysUser::getStatus, user.getStatus())
                .likeIfExists(SysUser::getPhonenumber, user.getPhonenumber())
                .nested(user.getDeptId() != null, i ->
                        i.eq(SysDept::getDeptId, user.getDeptId()).or()
                                .in(SysDept::getDeptId, SysDept.class, w ->
                                        w.setAlias("t").select(SysDept::getDeptId)
                                                .like(SysDept::getAncestors, "%," + user.getDeptId() + ",%")
                                                .or().like(SysDept::getAncestors, "%" + user.getDeptId() + ",%")
                                                .or().like(SysDept::getAncestors, "%," + user.getDeptId() + "%")
                                ));
        if (user.getParams() != null) {
            if (user.getParams().get("beginTime") != null) {
                wrapper.ge(SysUser::getCreateTime, user.getParams().get("beginTime"));
            }
            if (user.getParams().get("endTime") != null) {
                wrapper.le(SysUser::getCreateTime, user.getParams().get("endTime"));
            }
        }
        if (page != null) {
            selectJoinPage(page, wrapper);
            return page.getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param page 分页对象
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    default List<SysUser> selectAllocatedList(IPage<SysUser> page, SysUser user) {
        MPJLambdaWrapper<SysUser> wrapper = JoinWrappers.lambda(SysUser.class)
                .distinct().setAlias("u")
                .selectAll(SysUser.class)
                .leftJoin(SysDept.class, "d", SysDept::getDeptId, SysUser::getDeptId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getUserId, SysUser::getUserId)
                .leftJoin(SysRole.class, "r", SysRole::getRoleId, SysUserRole::getRoleId)
                .eq(SysUser::getDelFlag, "0")
                .eq(SysRole::getRoleId, user.getRoleId())
                .likeIfExists(SysUser::getUserName, user.getUserName())
                .likeIfExists(SysUser::getPhonenumber, user.getPhonenumber());
        if (page != null) {
            selectJoinPage(page, wrapper);
            return page.getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param page 分页对象
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    default List<SysUser> selectUnallocatedList(IPage<SysUser> page, SysUser user) {
        MPJLambdaWrapper<SysUser> wrapper = JoinWrappers.lambda(SysUser.class)
                .distinct().setAlias("u")
                .selectAll(SysUser.class)
                .leftJoin(SysDept.class, "d", SysDept::getDeptId, SysUser::getDeptId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getUserId, SysUser::getUserId)
                .leftJoin(SysRole.class, "r", SysRole::getRoleId, SysUserRole::getRoleId)
                .eq(SysUser::getDelFlag, "0")
                .and(w -> w.ne(SysRole::getRoleId, user.getRoleId())
                        .or().isNull(SysRole::getRoleId))
                .notIn(SysUser::getUserId, SysUser.class, w ->
                        w.select(SysUser::getUserId).innerJoin(SysUserRole.class, "ur", SysUserRole::getUserId, SysUser::getUserId).eq(SysUserRole::getRoleId, user.getRoleId()))
                .likeIfExists(SysUser::getUserName, user.getUserName())
                .likeIfExists(SysUser::getPhonenumber, user.getPhonenumber());
        if (page != null) {
            selectJoinPage(page, wrapper);
            return page.getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    default SysUser selectUserByUserName(String userName) {
        return selectJoinOne(getUserDetailWrapper().eq(SysUser::getUserName, userName));
    }

    default MPJLambdaWrapper<SysUser> getUserDetailWrapper() {
        return new MPJLambdaWrapper<SysUser>()
                .selectAll(SysUser.class)
                .selectAssociation(SysDept.class, SysUser::getDept, b -> b
                        .result(SysDept::getDeptId)
                        .result(SysDept::getParentId)
                        .result(SysDept::getDeptName)
                        .result(SysDept::getOrderNum)
                        .result(SysDept::getLeader)
                        .result(SysDept::getStatus))
                .selectCollection(SysRole.class, SysUser::getRoles, b -> b
                        .result(SysRole::getRoleId)
                        .result(SysRole::getRoleName)
                        .result(SysRole::getRoleKey)
                        .result(SysRole::getRoleSort)
                        .result(SysRole::getDataScope)
                        .result(SysRole::getStatus))
                .leftJoin(SysDept.class, "d", SysDept::getDeptId, SysUser::getDeptId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getUserId, SysUser::getUserId)
                .leftJoin(SysRole.class, "r", SysRole::getRoleId, SysUserRole::getRoleId);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    default SysUser selectUserById(Long userId) {
        return selectJoinOne(getUserDetailWrapper().eq(SysUser::getUserId, userId));
    }

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    default int insertUser(SysUser user) {
        return insert(user);
    }

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    default int updateUser(SysUser user) {
        return updateById(user);
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    default int updateUserAvatar(String userName, String avatar) {
        return update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .set(SysUser::getAvatar, avatar));
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    default int resetUserPwd(String userName, String password) {
        return update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)
                .set(SysUser::getPassword, password));
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    default int deleteUserById(Long userId) {
        return update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, userId)
                .set(SysUser::getDelFlag, "2"));
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    default int deleteUserByIds(Long[] userIds) {
        return update(null, new LambdaUpdateWrapper<SysUser>()
                .in(SysUser::getUserId, Arrays.asList(userIds))
                .set(SysUser::getDelFlag, "2"));
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     * @return 结果
     */
    default int checkUserNameUnique(String userName) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName)));
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param phonenumber 手机号码
     * @return 结果
     */
    default SysUser checkPhoneUnique(String phonenumber) {
        return selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getPhonenumber)
                .eq(SysUser::getPhonenumber, phonenumber)
                .last("LIMIT 1"));
    }

    /**
     * 校验email是否唯一
     *
     * @param email 用户邮箱
     * @return 结果
     */
    default SysUser checkEmailUnique(String email) {
        return selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getEmail)
                .eq(SysUser::getEmail, email)
                .last("LIMIT 1"));
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default int checkDeptExistUser(Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getDeptId, deptId);
        wrapper.eq(SysUser::getDelFlag, "0");
        return Math.toIntExact(selectCount(wrapper));
    }
}
