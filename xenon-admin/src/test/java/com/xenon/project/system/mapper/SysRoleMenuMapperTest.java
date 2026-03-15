package com.xenon.project.system.mapper;

import com.xenon.system.domain.SysRoleMenu;
import com.xenon.system.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysRoleMenuMapper 测试类
 * 测试角色与菜单关联表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysRoleMenuMapperTest {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 测试批量新增角色菜单信息
     */
    @Test
    @Order(1)
    void testBatchRoleMenu() {
        // 准备测试数据
        SysRoleMenu roleMenu1 = new SysRoleMenu();
        roleMenu1.setRoleId(999L);
        roleMenu1.setMenuId(1L);

        SysRoleMenu roleMenu2 = new SysRoleMenu();
        roleMenu2.setRoleId(999L);
        roleMenu2.setMenuId(2L);

        List<SysRoleMenu> roleMenuList = Arrays.asList(roleMenu1, roleMenu2);

        // 执行批量插入
        int result = sysRoleMenuMapper.batchRoleMenu(roleMenuList);

        // 验证结果
        assertEquals(2, result, "批量插入应返回2");

        // 清理测试数据
        sysRoleMenuMapper.deleteRoleMenuByRoleId(999L);
    }

    /**
     * 测试查询菜单使用数量
     */
    @Test
    @Order(2)
    void testCheckMenuExistRole() {
        // 先插入测试数据
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setRoleId(888L);
        roleMenu.setMenuId(100L);
        sysRoleMenuMapper.insert(roleMenu);

        // 执行查询
        int count = sysRoleMenuMapper.checkMenuExistRole(100L);

        // 验证结果
        assertTrue(count >= 1, "菜单使用数量应大于等于1");

        // 清理测试数据
        sysRoleMenuMapper.deleteRoleMenuByRoleId(888L);
    }

    /**
     * 测试通过角色ID删除角色和菜单关联
     */
    @Test
    @Order(3)
    void testDeleteRoleMenuByRoleId() {
        // 先插入测试数据
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setRoleId(777L);
        roleMenu.setMenuId(1L);
        sysRoleMenuMapper.insert(roleMenu);

        // 执行删除
        int result = sysRoleMenuMapper.deleteRoleMenuByRoleId(777L);

        // 验证删除成功
        assertTrue(result >= 1, "删除应成功");
    }

    /**
     * 测试批量删除角色菜单关联信息
     */
    @Test
    @Order(4)
    void testDeleteRoleMenu() {
        // 先插入测试数据
        SysRoleMenu roleMenu1 = new SysRoleMenu();
        roleMenu1.setRoleId(666L);
        roleMenu1.setMenuId(1L);
        sysRoleMenuMapper.insert(roleMenu1);

        SysRoleMenu roleMenu2 = new SysRoleMenu();
        roleMenu2.setRoleId(665L);
        roleMenu2.setMenuId(1L);
        sysRoleMenuMapper.insert(roleMenu2);

        // 执行批量删除
        int result = sysRoleMenuMapper.deleteRoleMenu(new Long[]{666L, 665L});

        // 验证删除成功
        assertTrue(result >= 2, "批量删除应成功删除至少2条记录");
    }

    /**
     * 测试 MyBatis-Plus BaseMapper 的 selectCount 方法
     */
    @Test
    @Order(5)
    void testSelectCount() {
        Long count = sysRoleMenuMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
        assertTrue(count >= 0, "查询数量应大于等于0");
    }
}
