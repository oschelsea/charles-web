package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charles.project.system.domain.SysRole;

import java.util.Arrays;
import java.util.List;

/**
 * 角色表 数据层
 *
 * @author charles
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    public List<SysRole> selectRoleList(SysRole role);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRole> selectRolePermissionByUserId(Long userId);

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
    public List<Integer> selectRoleListByUserId(Long userId);

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    public SysRole selectRoleById(Long roleId);

    /**
     * 根据用户ID查询角色
     *
     * @param userName 用户名
     * @return 角色列表
     */
    /**
     * 根据用户ID查询角色
     *
     * @param userName 用户名
     * @return 角色列表
     */
    public List<SysRole> selectRolesByUserName(String userName);

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
