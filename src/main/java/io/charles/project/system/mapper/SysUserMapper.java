package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charles.project.system.domain.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.Arrays;
import java.util.List;

/**
 * 用户表 数据层
 *
 * @author charles
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据条件分页查询用户列表
     *
     * @param sysUser 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    /**
     * 根据条件分页查询未已配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUser selectUserByUserName(String userName);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUser selectUserById(Long userId);

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
    default int updateUserAvatar(@Param("userName") String userName, @Param("avatar") String avatar) {
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
    default int resetUserPwd(@Param("userName") String userName, @Param("password") String password) {
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
}
