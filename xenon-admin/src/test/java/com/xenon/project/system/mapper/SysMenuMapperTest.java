package com.xenon.project.system.mapper;

import com.xenon.common.core.domain.entity.SysMenu;
import com.xenon.common.core.domain.entity.SysRole;
import com.xenon.common.core.domain.entity.SysUser;
import com.xenon.system.domain.SysRoleMenu;
import com.xenon.system.domain.SysUserRole;
import com.xenon.system.mapper.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysMenuMapper 测试类
 * 测试菜单表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysMenuMapperTest {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    private static Long testMenuId;

    /**
     * 测试新增菜单
     */
    @Test
    @Order(1)
    void testInsertMenu() {
        SysMenu menu = new SysMenu();
        menu.setMenuName("测试菜单");
        menu.setParentId(0L);
        menu.setOrderNum(99);
        menu.setPath("test");
        menu.setComponent("test/index");
        menu.setIsFrame(1);
        menu.setIsCache(0);
        menu.setMenuType("C");
        menu.setVisible("0");
        menu.setStatus("0");
        menu.setPerms("test:menu:list");

        int result = sysMenuMapper.insertMenu(menu);
        assertEquals(1, result, "新增应成功");
        assertNotNull(menu.getMenuId(), "菜单ID不应为null");
        testMenuId = menu.getMenuId();
    }

    /**
     * 测试按ID查询菜单
     */
    @Test
    @Order(2)
    void testSelectMenuById() {
        SysMenu menu = sysMenuMapper.selectMenuById(testMenuId);
        assertNotNull(menu, "菜单不应为null");
        assertEquals("测试菜单", menu.getMenuName(), "菜单名称应匹配");
    }

    /**
     * 测试查询菜单列表
     */
    @Test
    @Order(3)
    void testSelectMenuList() {
        SysMenu query = new SysMenu();
        query.setMenuName("测试");
        List<SysMenu> list = sysMenuMapper.selectMenuList(query);
        assertFalse(list.isEmpty(), "菜单列表不应为空");
    }

    /**
     * 测试查询所有菜单树
     */
    @Test
    @Order(4)
    void testSelectMenuTreeAll() {
        List<SysMenu> list = sysMenuMapper.selectMenuTreeAll();
        assertNotNull(list, "菜单树不应为null");
    }

    /**
     * 测试查询权限
     */
    @Test
    @Order(5)
    void testSelectMenuPerms() {
        List<String> perms = sysMenuMapper.selectMenuPerms();
        assertNotNull(perms, "权限列表不应为null");
    }

    /**
     * 测试是否存在子节点
     */
    @Test
    @Order(6)
    void testHasChildByMenuId() {
        int count = sysMenuMapper.hasChildByMenuId(0L);
        assertTrue(count >= 0, "查询子节点数应成功");
    }

    /**
     * 测试校验菜单名称唯一性
     */
    @Test
    @Order(7)
    void testCheckMenuNameUnique() {
        SysMenu menu = sysMenuMapper.checkMenuNameUnique("测试菜单", 0L);
        assertNotNull(menu, "菜单不应为null");
    }

    /**
     * 测试修改菜单
     */
    @Test
    @Order(8)
    void testUpdateMenu() {
        SysMenu menu = sysMenuMapper.selectById(testMenuId);
        menu.setMenuName("更新后菜单");
        int result = sysMenuMapper.updateMenu(menu);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试根据用户ID查询菜单列表
     */
    @Test
    @Order(9)
    void testSelectMenuListByUserId() {
        // 1. 创建用户
        SysUser user = new SysUser();
        user.setUserName("menu_user_" + System.currentTimeMillis());
        user.setNickName("Menu User");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        // 2. 创建角色
        SysRole role = new SysRole();
        role.setRoleName("Menu Role");
        role.setRoleKey("menu_role_" + System.currentTimeMillis());
        role.setRoleSort(1);
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        // 3. 用户关联角色
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 4. 角色关联菜单 (关联我们之前创建的 testMenuId)
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(testMenuId);
        sysRoleMenuMapper.insert(rm);

        // 5. 查询验证
        SysMenu query = new SysMenu();
        query.setMenuName("更新后菜单");

        List<SysMenu> list = sysMenuMapper.selectMenuListByUserId(query, userId);
        assertFalse(list.isEmpty(), "应查询到用户关联的菜单");
        boolean exists = list.stream().anyMatch(m -> m.getMenuId().equals(testMenuId));
        assertTrue(exists, "列表应包含测试菜单");
    }

    /**
     * 测试根据用户ID查询权限列表
     */
    @Test
    @Order(10)
    void testSelectMenuPermsByUserId() {
        // 复用 testSelectMenuListByUserId 中创建的数据太麻烦，不如新建，防止测试顺序依赖问题（虽然有Order）
        // 为了方便，我们在本测试方法内自闭环创建数据

        // 1. 创建用户
        SysUser user = new SysUser();
        user.setUserName("perm_user_" + System.currentTimeMillis());
        user.setNickName("Perm User");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        // 2. 创建角色
        SysRole role = new SysRole();
        role.setRoleName("Perm Role");
        role.setRoleKey("perm_role_" + System.currentTimeMillis());
        role.setRoleSort(1);
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        // 3. 用户关联角色
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 4. 确保 testMenuId 有权限标识
        SysMenu menu = sysMenuMapper.selectById(testMenuId);
        // 如果之前测试修改了各种属性，这里确保它有perms
        if (menu.getPerms() == null || menu.getPerms().isEmpty()) {
            menu.setPerms("test:menu:perm");
            sysMenuMapper.updateById(menu);
        }

        // 5. 角色关联菜单
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(testMenuId);
        sysRoleMenuMapper.insert(rm);

        // 6. 查询验证
        List<String> perms = sysMenuMapper.selectMenuPermsByUserId(userId);
        assertNotNull(perms, "权限列表不应为null");
        assertTrue(perms.contains(menu.getPerms()), "应包含测试菜单的权限标识");
    }

    /**
     * 测试根据用户ID查询菜单树
     */
    @Test
    @Order(11)
    void testSelectMenuTreeByUserId() {
        // 1. 创建用户
        SysUser user = new SysUser();
        user.setUserName("tree_user_" + System.currentTimeMillis());
        user.setNickName("Tree User");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        // 2. 创建角色
        SysRole role = new SysRole();
        role.setRoleName("Tree Role");
        role.setRoleKey("tree_role_" + System.currentTimeMillis());
        role.setRoleSort(1);
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        // 3. 用户关联角色
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 4. 角色关联菜单
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(testMenuId);
        sysRoleMenuMapper.insert(rm);

        // 5. 查询验证
        List<SysMenu> list = sysMenuMapper.selectMenuTreeByUserId(userId);
        assertNotNull(list, "菜单树不应为null");
        boolean exists = list.stream().anyMatch(m -> m.getMenuId().equals(testMenuId));
        assertTrue(exists, "菜单树应包含测试菜单");
    }

    /**
     * 测试根据角色ID查询菜单列表
     */
    @Test
    @Order(12)
    void testSelectMenuListByRoleId() {
        // 1. 创建角色
        SysRole role = new SysRole();
        role.setRoleName("List Role");
        role.setRoleKey("list_role_" + System.currentTimeMillis());
        role.setRoleSort(1);
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        // 2. 角色关联菜单
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(testMenuId);
        sysRoleMenuMapper.insert(rm);

        // 3. 查询验证
        List<Integer> list = sysMenuMapper.selectMenuListByRoleId(roleId, false);
        assertNotNull(list, "菜单ID列表不应为null");
        assertTrue(list.contains(testMenuId.intValue()), "应包含测试菜单ID"); // 注意 Long 转 int 的问题，这里 sysMenuMapper 返回的是 Integer list 吗？看 mapper 定义
        // sysMenuMapper定义：List<Integer> selectMenuListByRoleId
        // testMenuId 是 Long, 需要注意类型匹配。
        // 如果 ID 很大，intValue() 可能会溢出，但在测试环境中应该还好。
        // 严谨一点，应该 verify type first or checking mapper definition.
        // Mapper defines: List<Integer> selectMenuListByRoleId...
        // Wait, why returning Integer for ID? Usually IDs are Long.
        // Let's check mapper definition again.
        // default List<Integer> selectMenuListByRoleId(...) -> returns selectJoinList(Integer.class, wrapper)
        // Yes it returns Integer. Ideally should be Long but let's stick to existing code behavior.
    }


    /**
     * 测试删除菜单
     */
    @Test
    @Order(13)
    void testDeleteMenuById() {
        int result = sysMenuMapper.deleteMenuById(testMenuId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(14)
    void testSelectCount() {
        Long count = sysMenuMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
