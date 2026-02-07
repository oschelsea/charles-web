package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysRoleMapper 测试类
 * 测试角色表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysRoleMapperTest {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    private static Long testRoleId;

    /**
     * 测试新增角色
     */
    @Test
    @Order(1)
    void testInsertRole() {
        SysRole role = new SysRole();
        role.setRoleName("测试角色" + System.currentTimeMillis());
        role.setRoleKey("test_role_" + System.currentTimeMillis());
        role.setRoleSort("99");
        role.setDataScope("1");
        role.setStatus("0");

        int result = sysRoleMapper.insertRole(role);
        assertEquals(1, result, "新增应成功");
        assertNotNull(role.getRoleId(), "角色ID不应为null");
        testRoleId = role.getRoleId();
    }

    /**
     * 测试按ID查询角色
     */
    @Test
    @Order(2)
    void testSelectRoleById() {
        SysRole role = sysRoleMapper.selectRoleById(testRoleId);
        assertNotNull(role, "角色不应为null");
    }

    /**
     * 测试查询所有角色
     */
    @Test
    @Order(3)
    void testSelectRoleAll() {
        List<SysRole> list = sysRoleMapper.selectRoleAll();
        assertFalse(list.isEmpty(), "角色列表不应为空");
    }

    /**
     * 测试查询角色列表
     */
    @Test
    @Order(4)
    void testSelectRoleList() {
        SysRole query = new SysRole();
        query.setRoleName("测试");
        List<SysRole> list = sysRoleMapper.selectRoleList(query);
        assertFalse(list.isEmpty(), "角色列表不应为空");
    }

    /**
     * 测试修改角色
     */
    @Test
    @Order(5)
    void testUpdateRole() {
        SysRole role = sysRoleMapper.selectById(testRoleId);
        role.setRoleName("更新后角色");
        int result = sysRoleMapper.updateRole(role);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试删除角色
     */
    @Test
    @Order(6)
    void testDeleteRoleById() {
        int result = sysRoleMapper.deleteRoleById(testRoleId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除角色
     */
    @Test
    @Order(7)
    void testDeleteRoleByIds() {
        // 先插入测试数据
        SysRole role1 = new SysRole();
        role1.setRoleName("批量测试角色1_" + System.currentTimeMillis());
        role1.setRoleKey("batch_test1_" + System.currentTimeMillis());
        role1.setRoleSort("98");
        role1.setDataScope("1");
        role1.setStatus("0");
        sysRoleMapper.insertRole(role1);

        SysRole role2 = new SysRole();
        role2.setRoleName("批量测试角色2_" + System.currentTimeMillis());
        role2.setRoleKey("batch_test2_" + System.currentTimeMillis());
        role2.setRoleSort("97");
        role2.setDataScope("1");
        role2.setStatus("0");
        sysRoleMapper.insertRole(role2);

        // 执行批量删除
        int result = sysRoleMapper.deleteRoleByIds(new Long[]{role1.getRoleId(), role2.getRoleId()});
        assertTrue(result >= 2, "批量删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(8)
    void testSelectCount() {
        Long count = sysRoleMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
