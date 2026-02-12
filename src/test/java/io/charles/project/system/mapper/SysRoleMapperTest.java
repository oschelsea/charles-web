package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysRole;
import io.charles.project.system.domain.SysUser;
import io.charles.project.system.domain.SysUserRole;
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

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

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
        List<SysRole> list = sysRoleMapper.selectRoleList(null, query);
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

    /**
     * 测试根据用户ID查询角色权限
     */
    @Test
    @Order(9)
    void testSelectRolePermissionByUserId() {
        // 准备数据
        SysUser user = new SysUser();
        user.setUserName("test_user_perm_" + System.currentTimeMillis());
        user.setNickName("Test User Perm");
        sysUserMapper.insert(user);
        Long userId = user.getUserId();

        SysRole role = new SysRole();
        role.setRoleName("测试权限角色_" + System.currentTimeMillis());
        role.setRoleKey("test_role_perm_" + System.currentTimeMillis());
        role.setRoleSort("1");
        role.setDataScope("1");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 执行查询
        List<SysRole> roles = sysRoleMapper.selectRolePermissionByUserId(userId);
        assertFalse(roles.isEmpty(), "应查询到角色权限");
        boolean exists = roles.stream().anyMatch(r -> r.getRoleKey().equals(role.getRoleKey()));
        assertTrue(exists, "应包含刚插入的角色");
    }

    /**
     * 测试根据用户ID查询角色ID列表
     */
    @Test
    @Order(10)
    void testSelectRoleListByUserId() {
        // 准备数据
        SysUser user = new SysUser();
        user.setUserName("test_user_list_" + System.currentTimeMillis());
        user.setNickName("Test User List");
        sysUserMapper.insert(user);
        Long userId = user.getUserId();

        SysRole role = new SysRole();
        role.setRoleName("测试列表角色_" + System.currentTimeMillis());
        role.setRoleKey("test_role_list_" + System.currentTimeMillis());
        role.setRoleSort("2");
        role.setDataScope("1");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 执行查询
        List<Integer> roleIds = sysRoleMapper.selectRoleListByUserId(userId);
        assertFalse(roleIds.isEmpty(), "应查询到角色ID列表");
        // 注意：selectRoleListByUserId 返回的是 List<Integer>，而 roleId 是 Long
        // 这里假设 Mapper 返回的是 Integer (虽然 id 是 Long，但 Mapper 定义是 Integer)
        // 如果数据库 ID 很大，Integer 可能会溢出，但测试数据应该没事
        assertTrue(roleIds.contains(roleId.intValue()), "应包含刚插入的角色ID");
    }

    /**
     * 测试根据用户名查询角色
     */
    @Test
    @Order(11)
    void testSelectRolesByUserName() {
        // 准备数据
        String userName = "test_user_name_" + System.currentTimeMillis();
        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setNickName("Test User Name");
        sysUserMapper.insert(user);
        Long userId = user.getUserId();

        SysRole role = new SysRole();
        role.setRoleName("测试用户名角色_" + System.currentTimeMillis());
        role.setRoleKey("test_role_name_" + System.currentTimeMillis());
        role.setRoleSort("3");
        role.setDataScope("1");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 执行查询
        List<SysRole> roles = sysRoleMapper.selectRolesByUserName(userName);
        assertFalse(roles.isEmpty(), "应查询到角色");
        boolean exists = roles.stream().anyMatch(r -> r.getRoleId().equals(roleId));
        assertTrue(exists, "应包含刚插入的角色");
    }

    /**
     * 测试校验角色名称是否唯一
     */
    @Test
    @Order(12)
    void testCheckRoleNameUnique() {
        String roleName = "唯一名称测试_" + System.currentTimeMillis();
        SysRole role = new SysRole();
        role.setRoleName(roleName);
        role.setRoleKey("unique_name_" + System.currentTimeMillis());
        role.setRoleSort("4");
        role.setDataScope("1");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);

        SysRole result = sysRoleMapper.checkRoleNameUnique(roleName);
        assertNotNull(result, "应查询到角色");
        assertEquals(role.getRoleId(), result.getRoleId(), "ID应一致");

        SysRole notFound = sysRoleMapper.checkRoleNameUnique("不存在的名称_" + System.currentTimeMillis());
        assertNull(notFound, "不存在的角色应返回null");
    }

    /**
     * 测试校验角色权限是否唯一
     */
    @Test
    @Order(13)
    void testCheckRoleKeyUnique() {
        String roleKey = "unique_key_" + System.currentTimeMillis();
        SysRole role = new SysRole();
        role.setRoleName("唯一权限测试_" + System.currentTimeMillis());
        role.setRoleKey(roleKey);
        role.setRoleSort("5");
        role.setDataScope("1");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);

        SysRole result = sysRoleMapper.checkRoleKeyUnique(roleKey);
        assertNotNull(result, "应查询到角色");
        assertEquals(role.getRoleId(), result.getRoleId(), "ID应一致");

        SysRole notFound = sysRoleMapper.checkRoleKeyUnique("non_existent_key_" + System.currentTimeMillis());
        assertNull(notFound, "不存在的角色应返回null");
    }
}
