package com.xenon.project.system.mapper;

import com.xenon.system.domain.SysUserRole;
import com.xenon.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserRoleMapper 测试类
 * 测试用户与角色关联表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysUserRoleMapperTest {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 测试批量新增用户角色信息
     */
    @Test
    @Order(1)
    void testBatchUserRole() {
        // 准备测试数据
        SysUserRole userRole1 = new SysUserRole();
        userRole1.setUserId(999L);
        userRole1.setRoleId(1L);

        SysUserRole userRole2 = new SysUserRole();
        userRole2.setUserId(999L);
        userRole2.setRoleId(2L);

        List<SysUserRole> userRoleList = Arrays.asList(userRole1, userRole2);

        // 执行批量插入
        int result = sysUserRoleMapper.batchUserRole(userRoleList);

        // 验证结果
        assertEquals(2, result, "批量插入应返回2");

        // 清理测试数据
        sysUserRoleMapper.deleteUserRoleByUserId(999L);
    }

    /**
     * 测试通过角色ID查询角色使用数量
     */
    @Test
    @Order(2)
    void testCountUserRoleByRoleId() {
        // 先插入测试数据
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(888L);
        userRole.setRoleId(100L);
        sysUserRoleMapper.insert(userRole);

        // 执行查询
        int count = sysUserRoleMapper.countUserRoleByRoleId(100L);

        // 验证结果
        assertTrue(count >= 1, "角色使用数量应大于等于1");

        // 清理测试数据
        sysUserRoleMapper.deleteUserRoleByUserId(888L);
    }

    /**
     * 测试通过用户ID删除用户和角色关联
     */
    @Test
    @Order(3)
    void testDeleteUserRoleByUserId() {
        // 先插入测试数据
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(777L);
        userRole.setRoleId(1L);
        sysUserRoleMapper.insert(userRole);

        // 执行删除
        int result = sysUserRoleMapper.deleteUserRoleByUserId(777L);

        // 验证删除成功
        assertTrue(result >= 1, "删除应成功");
    }

    /**
     * 测试批量删除用户和角色关联
     */
    @Test
    @Order(4)
    void testDeleteUserRole() {
        // 先插入测试数据
        SysUserRole userRole1 = new SysUserRole();
        userRole1.setUserId(666L);
        userRole1.setRoleId(1L);
        sysUserRoleMapper.insert(userRole1);

        SysUserRole userRole2 = new SysUserRole();
        userRole2.setUserId(665L);
        userRole2.setRoleId(1L);
        sysUserRoleMapper.insert(userRole2);

        // 执行批量删除
        int result = sysUserRoleMapper.deleteUserRole(new Long[]{666L, 665L});

        // 验证删除成功
        assertTrue(result >= 2, "批量删除应成功删除至少2条记录");
    }

    /**
     * 测试删除用户和角色关联信息
     */
    @Test
    @Order(5)
    void testDeleteUserRoleInfo() {
        // 先插入测试数据
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(555L);
        userRole.setRoleId(50L);
        sysUserRoleMapper.insert(userRole);

        // 执行删除
        int result = sysUserRoleMapper.deleteUserRoleInfo(userRole);

        // 验证删除成功
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量取消授权用户角色
     */
    @Test
    @Order(6)
    void testDeleteUserRoleInfos() {
        // 先插入测试数据
        SysUserRole userRole1 = new SysUserRole();
        userRole1.setUserId(444L);
        userRole1.setRoleId(99L);
        sysUserRoleMapper.insert(userRole1);

        SysUserRole userRole2 = new SysUserRole();
        userRole2.setUserId(443L);
        userRole2.setRoleId(99L);
        sysUserRoleMapper.insert(userRole2);

        // 执行批量取消授权
        int result = sysUserRoleMapper.deleteUserRoleInfos(99L, new Long[]{444L, 443L});

        // 验证删除成功
        assertTrue(result >= 2, "批量取消授权应成功");
    }
}
