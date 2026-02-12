package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysDept;
import io.charles.project.system.domain.SysRole;
import io.charles.project.system.domain.SysUser;
import io.charles.project.system.domain.SysUserRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserMapper 测试类
 * 测试用户表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysUserMapperTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    private static Long testUserId;

    /**
     * 测试新增用户
     */
    @Test
    @Order(1)
    void testInsertUser() {
        SysUser user = new SysUser();
        user.setUserName("testuser" + System.currentTimeMillis());
        user.setNickName("测试用户");
        user.setPassword("$2a$10$testpassword");
        user.setDeptId(100L);
        user.setEmail("test@test.com");
        user.setPhonenumber("13800000001");
        user.setSex("0");
        user.setStatus("0");

        int result = sysUserMapper.insertUser(user);
        assertEquals(1, result, "新增应成功");
        assertNotNull(user.getUserId(), "用户ID不应为null");
        testUserId = user.getUserId();
    }

    /**
     * 测试按ID查询用户
     */
    @Test
    @Order(2)
    void testSelectUserById() {
        SysUser user = sysUserMapper.selectUserById(testUserId);
        assertNotNull(user, "用户不应为null");
        assertEquals("测试用户", user.getNickName(), "昵称应匹配");
    }

    /**
     * 测试查询用户列表
     */
    @Test
    @Order(3)
    void testSelectUserList() {
        SysUser query = new SysUser();
        query.setNickName("测试");
        List<SysUser> list = sysUserMapper.selectUserList(null, query);
        assertFalse(list.isEmpty(), "用户列表不应为空");
    }

    /**
     * 测试校验用户名唯一
     */
    @Test
    @Order(4)
    void testCheckUserNameUnique() {
        SysUser user = sysUserMapper.selectUserById(testUserId);
        int count = sysUserMapper.checkUserNameUnique(user.getUserName());
        assertTrue(count >= 1, "用户名应存在");
    }

    /**
     * 测试修改用户
     */
    @Test
    @Order(5)
    void testUpdateUser() {
        SysUser user = sysUserMapper.selectById(testUserId);
        user.setNickName("更新后用户");
        int result = sysUserMapper.updateUser(user);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试重置用户密码
     */
    @Test
    @Order(6)
    void testResetUserPwd() {
        SysUser user = sysUserMapper.selectUserById(testUserId);
        int result = sysUserMapper.resetUserPwd(user.getUserName(), "$2a$10$newpassword");
        assertEquals(1, result, "重置密码应成功");
    }

    /**
     * 测试删除用户
     */
    @Test
    @Order(7)
    void testDeleteUserById() {
        int result = sysUserMapper.deleteUserById(testUserId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除用户
     */
    @Test
    @Order(8)
    void testDeleteUserByIds() {
        // 先插入测试数据
        SysUser user1 = new SysUser();
        user1.setUserName("batchtest1" + System.currentTimeMillis());
        user1.setNickName("批量测试1");
        user1.setPassword("test");
        user1.setDeptId(100L);
        sysUserMapper.insertUser(user1);

        SysUser user2 = new SysUser();
        user2.setUserName("batchtest2" + System.currentTimeMillis());
        user2.setNickName("批量测试2");
        user2.setPassword("test");
        user2.setDeptId(100L);
        sysUserMapper.insertUser(user2);

        // 执行批量删除
        int result = sysUserMapper.deleteUserByIds(new Long[]{user1.getUserId(), user2.getUserId()});
        assertTrue(result >= 2, "批量删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(9)
    void testSelectCount() {
        Long count = sysUserMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }

    /**
     * 测试查询已分配用户角色列表
     */
    @Test
    @Order(10)
    void testSelectAllocatedList() {
        // 数据准备
        SysUser user = new SysUser();
        user.setUserName("alloc_user_" + System.currentTimeMillis());
        user.setNickName("Alloc User");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        SysRole role = new SysRole();
        role.setRoleName("Alloc Role");
        role.setRoleKey("alloc_role_" + System.currentTimeMillis());
        role.setRoleSort("1");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        sysUserRoleMapper.insert(ur);

        // 执行查询
        SysUser query = new SysUser();
        query.setRoleId(roleId);
        List<SysUser> list = sysUserMapper.selectAllocatedList(null, query);
        assertFalse(list.isEmpty(), "应查询到已分配用户");
        boolean exists = list.stream().anyMatch(u -> u.getUserId().equals(userId));
        assertTrue(exists, "应包含刚分配的用户");
    }

    /**
     * 测试查询未分配用户角色列表
     */
    @Test
    @Order(11)
    void testSelectUnallocatedList() {
        // 数据准备 - 创建一个用户但不分配特定角色
        SysUser user = new SysUser();
        user.setUserName("unalloc_user_" + System.currentTimeMillis());
        user.setNickName("Unalloc User");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        SysRole role = new SysRole();
        role.setRoleName("Unalloc Role");
        role.setRoleKey("unalloc_role_" + System.currentTimeMillis());
        role.setRoleSort("2");
        role.setStatus("0");
        sysRoleMapper.insertRole(role);
        Long roleId = role.getRoleId();

        // 此时 user 未分配给 role

        // 执行查询
        SysUser query = new SysUser();
        query.setRoleId(roleId);
        List<SysUser> list = sysUserMapper.selectUnallocatedList(null, query);
        assertFalse(list.isEmpty(), "应查询到未分配用户");
        boolean exists = list.stream().anyMatch(u -> u.getUserId().equals(userId));
        assertTrue(exists, "应包含未分配的用户");
    }

    /**
     * 测试修改用户头像
     */
    @Test
    @Order(12)
    void testUpdateUserAvatar() {
        SysUser user = sysUserMapper.selectUserById(testUserId);
        String newAvatar = "avatar_path_" + System.currentTimeMillis();
        int result = sysUserMapper.updateUserAvatar(user.getUserName(), newAvatar);
        assertEquals(1, result, "修改头像应成功");

        SysUser updatedUser = sysUserMapper.selectUserById(testUserId);
        assertEquals(newAvatar, updatedUser.getAvatar(), "头像应更新");
    }

    /**
     * 测试校验手机号码是否唯一
     */
    @Test
    @Order(13)
    void testCheckPhoneUnique() {
        String phone = "139" + System.currentTimeMillis() % 100000000;
        SysUser user = new SysUser();
        user.setUserName("phone_user_" + System.currentTimeMillis());
        user.setPhonenumber(phone);
        user.setNickName("Phone User");
        sysUserMapper.insertUser(user);

        SysUser result = sysUserMapper.checkPhoneUnique(phone);
        assertNotNull(result, "应查询到用户");
        assertEquals(user.getUserId(), result.getUserId(), "ID应一致");
    }

    /**
     * 测试校验email是否唯一
     */
    @Test
    @Order(14)
    void testCheckEmailUnique() {
        String email = "unique_" + System.currentTimeMillis() + "@test.com";
        SysUser user = new SysUser();
        user.setUserName("email_user_" + System.currentTimeMillis());
        user.setEmail(email);
        user.setNickName("Email User");
        sysUserMapper.insertUser(user);

        SysUser result = sysUserMapper.checkEmailUnique(email);
        assertNotNull(result, "应查询到用户");
        assertEquals(user.getUserId(), result.getUserId(), "ID应一致");
    }

    /**
     * 测试查询部门是否存在用户
     */
    @Test
    @Order(15)
    void testCheckDeptExistUser() {
        // 创建一个部门
        SysDept dept = new SysDept();
        dept.setDeptName("Test Dept " + System.currentTimeMillis());
        dept.setParentId(100L); // 假设100是存在的父ID
        dept.setOrderNum("1");
        dept.setStatus("0");
        sysDeptMapper.insertDept(dept);
        Long deptId = dept.getDeptId();

        // 创建一个用户属于该部门
        SysUser user = new SysUser();
        user.setUserName("dept_user_" + System.currentTimeMillis());
        user.setDeptId(deptId);
        user.setNickName("Dept User");
        sysUserMapper.insertUser(user);

        int count = sysUserMapper.checkDeptExistUser(deptId);
        assertTrue(count > 0, "部门下应存在用户");

        int zeroCount = sysUserMapper.checkDeptExistUser(deptId + 99999); // 不存在的部门
        assertEquals(0, zeroCount, "不存在的部门应返回0");
    }
}
