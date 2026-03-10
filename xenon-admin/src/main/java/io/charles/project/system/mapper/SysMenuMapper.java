package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.charles.common.utils.StringUtils;
import io.charles.project.system.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单表 数据层
 *
 * @author charles
 */
public interface SysMenuMapper extends MPJBaseMapper<SysMenu> {
    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    default List<SysMenu> selectMenuList(SysMenu menu) {
        return selectList(new LambdaQueryWrapper<SysMenu>()
                .like(StringUtils.isNotEmpty(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
                .eq(StringUtils.isNotEmpty(menu.getVisible()), SysMenu::getVisible, menu.getVisible())
                .eq(StringUtils.isNotEmpty(menu.getStatus()), SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum));
    }

    /**
     * 根据用户所有权限
     *
     * @return 权限列表
     */
    default List<String> selectMenuPerms() {
        return selectJoinList(String.class, new MPJLambdaWrapper<SysMenu>()
                .distinct()
                .select(SysMenu::getPerms)
                .leftJoin(SysRoleMenu.class, "rm", SysRoleMenu::getMenuId, SysMenu::getMenuId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getRoleId, SysRoleMenu::getRoleId));
    }

    /**
     * 根据用户查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    default List<SysMenu> selectMenuListByUserId(SysMenu menu, Long userId) {
        return selectJoinList(SysMenu.class, new MPJLambdaWrapper<SysMenu>()
                .distinct()
                .selectAll(SysMenu.class)
                .leftJoin(SysRoleMenu.class, "rm", SysRoleMenu::getMenuId, SysMenu::getMenuId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getRoleId, SysRoleMenu::getRoleId)
                .leftJoin(SysRole.class, "ro", SysRole::getRoleId, SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, userId)
                .likeIfExists(SysMenu::getMenuName, menu.getMenuName())
                .eqIfExists(SysMenu::getVisible, menu.getVisible())
                .eqIfExists(SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum));
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    default List<String> selectMenuPermsByUserId(Long userId) {
        return selectJoinList(String.class, new MPJLambdaWrapper<SysMenu>()
                .distinct()
                .select(SysMenu::getPerms)
                .leftJoin(SysRoleMenu.class, "rm", SysRoleMenu::getMenuId, SysMenu::getMenuId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getRoleId, SysRoleMenu::getRoleId)
                .leftJoin(SysRole.class, "r", SysRole::getRoleId, SysUserRole::getRoleId)
                .eq(SysMenu::getStatus, "0")
                .eq(SysRole::getStatus, "0")
                .eq(SysUserRole::getUserId, userId));
    }

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    default List<SysMenu> selectMenuTreeAll() {
        return selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getMenuType, "M", "C")
                .eq(SysMenu::getStatus, "0")
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum));
    }

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    default List<SysMenu> selectMenuTreeByUserId(Long userId) {
        return selectJoinList(SysMenu.class, new MPJLambdaWrapper<SysMenu>()
                .distinct().setAlias("m")
                .selectAll(SysMenu.class)
                .leftJoin(SysRoleMenu.class, "rm", SysRoleMenu::getMenuId, SysMenu::getMenuId)
                .leftJoin(SysUserRole.class, "ur", SysUserRole::getRoleId, SysRoleMenu::getRoleId)
                .leftJoin(SysRole.class, "ro", SysRole::getRoleId, SysUserRole::getRoleId)
                .leftJoin(SysUser.class, "u", SysUser::getUserId, SysUserRole::getUserId)
                .eq(SysUser::getUserId, userId)
                .in(SysMenu::getMenuType, "M", "C")
                .eq(SysMenu::getStatus, "0")
                .eq(SysRole::getStatus, "0")
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum));
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId            角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return 选中菜单列表
     */
    default List<Integer> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly) {
        MPJLambdaWrapper<SysMenu> wrapper = new MPJLambdaWrapper<SysMenu>()
                .select(SysMenu::getMenuId).setAlias("m")
                .leftJoin(SysRoleMenu.class, "rm", SysRoleMenu::getMenuId, SysMenu::getMenuId)
                .eq(SysRoleMenu::getRoleId, roleId);
        if (menuCheckStrictly) {
            wrapper.notIn(SysMenu::getMenuId, SysMenu.class, w ->
                    w.select(SysMenu::getParentId).innerJoin(SysRoleMenu.class, "rm", SysRoleMenu::getMenuId, SysMenu::getMenuId).eq(SysRoleMenu::getRoleId, roleId));
        }
        wrapper.orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum);
        return selectJoinList(Integer.class, wrapper);
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    default SysMenu selectMenuById(Long menuId) {
        return selectById(menuId);
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    default int hasChildByMenuId(Long menuId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId)));
    }

    /**
     * 新增菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    default int insertMenu(SysMenu menu) {
        return insert(menu);
    }

    /**
     * 修改菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    default int updateMenu(SysMenu menu) {
        return updateById(menu);
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    default int deleteMenuById(Long menuId) {
        return deleteById(menuId);
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @return 结果
     */
    default SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId) {
        return selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuName, menuName)
                .eq(SysMenu::getParentId, parentId)
                .last("limit 1"));
    }
}
