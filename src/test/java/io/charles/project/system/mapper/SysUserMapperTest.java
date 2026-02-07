package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysUser;
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
        List<SysUser> list = sysUserMapper.selectUserList(query);
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
}
