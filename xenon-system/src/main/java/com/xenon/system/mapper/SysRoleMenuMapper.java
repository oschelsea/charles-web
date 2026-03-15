package com.xenon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.system.domain.SysRoleMenu;
import org.apache.ibatis.executor.BatchResult;

import java.util.Arrays;
import java.util.List;

/**
 * 角色与菜单关联表 数据层
 *
 * @author charles
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    default int checkMenuExistRole(Long menuId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, menuId)));
    }

    /**
     * 通过角色ID删除角色和菜单关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    default int deleteRoleMenuByRoleId(Long roleId) {
        return delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
    }

    /**
     * 批量删除角色菜单关联信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    default int deleteRoleMenu(Long[] ids) {
        return delete(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, Arrays.asList(ids)));
    }

    /**
     * 批量新增角色菜单信息
     *
     * @param roleMenuList 角色菜单列表
     * @return 结果
     */
    default int batchRoleMenu(List<SysRoleMenu> roleMenuList) {
        List<BatchResult> results = insert(roleMenuList);
        return results.stream().map(t -> Arrays.stream(t.getUpdateCounts()).sum()).mapToInt(Integer::intValue).sum();
    }
}
