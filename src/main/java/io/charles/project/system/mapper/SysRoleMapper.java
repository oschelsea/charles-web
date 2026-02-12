package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.charles.project.system.domain.SysDept;
import io.charles.project.system.domain.SysRole;
import io.charles.project.system.domain.SysUser;
import io.charles.project.system.domain.SysUserRole;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色表 数据层
 *
 * @author charles
 */
public interface SysRoleMapper extends MPJBaseMapper<SysRole> {
    /**
     * 根据条件分页查询角色数据
     *
     * @param page 分页对象
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    default List<SysRole> selectRoleList(IPage<SysRole> page, SysRole role) {
        MPJLambdaWrapper<SysRole> wrapper = JoinWrappers.lambda();
        wrapper.distinct()
                .setAlias("r")
                .selectAll(SysRole.class)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getRoleId, SysRole::getRoleId)
                .leftJoin(SysUser.class, "u", SysUser::getUserId, SysUserRole::getUserId)
                .leftJoin(SysDept.class, "d", SysDept::getDeptId, SysUser::getDeptId)
                .eq(SysRole::getDelFlag, "0")
                .eqIfExists(SysRole::getRoleId, role.getRoleId())
                .likeIfExists(SysRole::getRoleName, role.getRoleName())
                .eqIfExists(SysRole::getStatus, role.getStatus())
                .likeIfExists(SysRole::getRoleKey, role.getRoleKey());

        if (role.getParams() != null) {
            String beginTime = (String) role.getParams().get("beginTime");
            String endTime = (String) role.getParams().get("endTime");
            if (beginTime != null) {
                wrapper.ge(SysRole::getCreateTime, beginTime);
            }
            if (endTime != null) {
                wrapper.le(SysRole::getCreateTime, endTime);
            }
        }

        wrapper.orderByAsc(SysRole::getRoleSort);

        if (page != null) {
            selectJoinPage(page, wrapper);
            return page.getRecords();
        }
        return selectJoinList(wrapper);
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    default List<SysRole> selectRolePermissionByUserId(Long userId) {
        MPJLambdaWrapper<SysRole> wrapper = JoinWrappers.lambda();
        wrapper.distinct()
                .selectAll(SysRole.class)
                .leftJoin(SysUserRole.class, SysUserRole::getRoleId, SysRole::getRoleId)
                .leftJoin(SysUser.class, SysUser::getUserId, SysUserRole::getUserId)
                .eq(SysRole::getDelFlag, "0")
                .eq(SysUser::getUserId, userId);
        return selectJoinList(wrapper);
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    default List<SysRole> selectRoleAll() {
        return selectList(null);
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    default List<Integer> selectRoleListByUserId(Long userId) {
        MPJLambdaWrapper<SysRole> wrapper = JoinWrappers.lambda();
        wrapper.distinct()
                .select(SysRole::getRoleId)
                .leftJoin(SysUserRole.class, SysUserRole::getRoleId, SysRole::getRoleId)
                .eq(SysUserRole::getUserId, userId);
        return selectObjs(wrapper).stream().map(o -> Integer.parseInt(o.toString())).collect(Collectors.toList());
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    default SysRole selectRoleById(Long roleId) {
        return selectById(roleId);
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userName 用户名
     * @return 角色列表
     */
    default List<SysRole> selectRolesByUserName(String userName) {
        MPJLambdaWrapper<SysRole> wrapper = JoinWrappers.lambda();
        wrapper.distinct()
                .selectAll(SysRole.class)
                .leftJoin(SysUserRole.class, SysUserRole::getRoleId, SysRole::getRoleId)
                .leftJoin(SysUser.class, SysUser::getUserId, SysUserRole::getUserId)
                .eq(SysRole::getDelFlag, "0")
                .eq(SysUser::getUserName, userName);
        return selectJoinList(wrapper);
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param roleName 角色名称
     * @return 角色信息
     */
    default SysRole checkRoleNameUnique(String roleName) {
        return selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleName, roleName)
                .last("LIMIT 1"));
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param roleKey 角色权限
     * @return 角色信息
     */
    default SysRole checkRoleKeyUnique(String roleKey) {
        return selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey)
                .last("LIMIT 1"));
    }

    /**
     * 修改角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    default int updateRole(SysRole role) {
        return updateById(role);
    }

    /**
     * 新增角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    default int insertRole(SysRole role) {
        return insert(role);
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    default int deleteRoleById(Long roleId) {
        return update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getRoleId, roleId)
                .set(SysRole::getDelFlag, "2"));
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    default int deleteRoleByIds(Long[] roleIds) {
        return update(null, new LambdaUpdateWrapper<SysRole>()
                .in(SysRole::getRoleId, Arrays.asList(roleIds))
                .set(SysRole::getDelFlag, "2"));
    }
}
