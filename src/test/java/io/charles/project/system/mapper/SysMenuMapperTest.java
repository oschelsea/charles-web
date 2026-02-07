package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysMenu;
import org.junit.jupiter.api.*;
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
        menu.setOrderNum("99");
        menu.setPath("test");
        menu.setComponent("test/index");
        menu.setIsFrame("1");
        menu.setIsCache("0");
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
     * 测试删除菜单
     */
    @Test
    @Order(9)
    void testDeleteMenuById() {
        int result = sysMenuMapper.deleteMenuById(testMenuId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(10)
    void testSelectCount() {
        Long count = sysMenuMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
