package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysDept;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysDeptMapper 测试类
 * 测试部门表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysDeptMapperTest {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    private static Long testDeptId;

    /**
     * 测试新增部门
     */
    @Test
    @Order(1)
    void testInsertDept() {
        SysDept dept = new SysDept();
        dept.setParentId(100L);
        dept.setAncestors("0,100");
        dept.setDeptName("测试部门");
        dept.setOrderNum("99");
        dept.setLeader("张三");
        dept.setPhone("13800000001");
        dept.setEmail("test@test.com");
        dept.setStatus("0");

        int result = sysDeptMapper.insertDept(dept);
        assertEquals(1, result, "新增应成功");
        assertNotNull(dept.getDeptId(), "部门ID不应为null");
        testDeptId = dept.getDeptId();
    }

    /**
     * 测试按ID查询部门
     */
    @Test
    @Order(2)
    void testSelectDeptById() {
        SysDept dept = sysDeptMapper.selectDeptById(testDeptId);
        assertNotNull(dept, "部门不应为null");
        assertEquals("测试部门", dept.getDeptName(), "部门名称应匹配");
    }

    /**
     * 测试查询部门列表
     */
    @Test
    @Order(3)
    void testSelectDeptList() {
        SysDept query = new SysDept();
        query.setDeptName("测试");
        List<SysDept> list = sysDeptMapper.selectDeptList(query);
        assertFalse(list.isEmpty(), "部门列表不应为空");
    }

    /**
     * 测试是否存在子节点
     */
    @Test
    @Order(4)
    void testHasChildByDeptId() {
        int count = sysDeptMapper.hasChildByDeptId(100L);
        assertTrue(count >= 0, "查询子节点数应成功");
    }

    /**
     * 测试校验部门名称唯一性
     */
    @Test
    @Order(5)
    void testCheckDeptNameUnique() {
        SysDept dept = sysDeptMapper.checkDeptNameUnique("测试部门", 100L);
        assertNotNull(dept, "部门不应为null");
    }

    /**
     * 测试修改部门
     */
    @Test
    @Order(6)
    void testUpdateDept() {
        SysDept dept = sysDeptMapper.selectById(testDeptId);
        dept.setDeptName("更新后部门");
        int result = sysDeptMapper.updateDept(dept);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试删除部门
     */
    @Test
    @Order(7)
    void testDeleteDeptById() {
        int result = sysDeptMapper.deleteDeptById(testDeptId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(8)
    void testSelectCount() {
        Long count = sysDeptMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }

    /**
     * 测试根据ID查询所有子部门
     */
    @Test
    @Order(9)
    void testSelectChildrenDeptById() {
        // 100 为核心祖先节点 ID
        List<SysDept> children = sysDeptMapper.selectChildrenDeptById(100L);
        assertNotNull(children, "子部门列表不应为null");
        assertTrue(children.size() > 0, "应查询到子部门");
    }

    /**
     * 测试根据ID查询所有子部门（正常状态）
     */
    @Test
    @Order(10)
    void testSelectNormalChildrenDeptById() {
        int count = sysDeptMapper.selectNormalChildrenDeptById(100L);
        assertTrue(count >= 0, "正常状态子部门数应大于等于0");
    }
}
