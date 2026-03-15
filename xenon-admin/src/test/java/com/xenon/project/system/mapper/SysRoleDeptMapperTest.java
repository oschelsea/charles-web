package com.xenon.project.system.mapper;

import com.xenon.system.domain.SysRoleDept;
import com.xenon.system.mapper.SysRoleDeptMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysRoleDeptMapper 测试类
 * 测试角色与部门关联表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysRoleDeptMapperTest {

    @Autowired
    private SysRoleDeptMapper sysRoleDeptMapper;

    /**
     * 测试批量新增角色部门信息
     */
    @Test
    @Order(1)
    void testBatchRoleDept() {
        // 准备测试数据
        SysRoleDept roleDept1 = new SysRoleDept();
        roleDept1.setRoleId(999L);
        roleDept1.setDeptId(1L);

        SysRoleDept roleDept2 = new SysRoleDept();
        roleDept2.setRoleId(999L);
        roleDept2.setDeptId(2L);

        List<SysRoleDept> roleDeptList = Arrays.asList(roleDept1, roleDept2);

        // 执行批量插入
        int result = sysRoleDeptMapper.batchRoleDept(roleDeptList);

        // 验证结果
        assertEquals(2, result, "批量插入应返回2");

        // 清理测试数据
        sysRoleDeptMapper.deleteRoleDeptByRoleId(999L);
    }

    /**
     * 测试查询部门使用数量
     */
    @Test
    @Order(2)
    void testSelectCountRoleDeptByDeptId() {
        // 先插入测试数据
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setRoleId(888L);
        roleDept.setDeptId(100L);
        sysRoleDeptMapper.insert(roleDept);

        // 执行查询
        int count = sysRoleDeptMapper.selectCountRoleDeptByDeptId(100L);

        // 验证结果
        assertTrue(count >= 1, "部门使用数量应大于等于1");

        // 清理测试数据
        sysRoleDeptMapper.deleteRoleDeptByRoleId(888L);
    }

    /**
     * 测试通过角色ID删除角色和部门关联
     */
    @Test
    @Order(3)
    void testDeleteRoleDeptByRoleId() {
        // 先插入测试数据
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setRoleId(777L);
        roleDept.setDeptId(1L);
        sysRoleDeptMapper.insert(roleDept);

        // 执行删除
        int result = sysRoleDeptMapper.deleteRoleDeptByRoleId(777L);

        // 验证删除成功
        assertTrue(result >= 1, "删除应成功");
    }

    /**
     * 测试批量删除角色部门关联信息
     */
    @Test
    @Order(4)
    void testDeleteRoleDept() {
        // 先插入测试数据
        SysRoleDept roleDept1 = new SysRoleDept();
        roleDept1.setRoleId(666L);
        roleDept1.setDeptId(1L);
        sysRoleDeptMapper.insert(roleDept1);

        SysRoleDept roleDept2 = new SysRoleDept();
        roleDept2.setRoleId(665L);
        roleDept2.setDeptId(1L);
        sysRoleDeptMapper.insert(roleDept2);

        // 执行批量删除
        int result = sysRoleDeptMapper.deleteRoleDept(new Long[]{666L, 665L});

        // 验证删除成功
        assertTrue(result >= 2, "批量删除应成功删除至少2条记录");
    }

    /**
     * 测试 MyBatis-Plus BaseMapper 的 selectCount 方法
     */
    @Test
    @Order(5)
    void testSelectCount() {
        Long count = sysRoleDeptMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
        assertTrue(count >= 0, "查询数量应大于等于0");
    }
}
