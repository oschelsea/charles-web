package io.charles.project.tool.gen.mapper;

import io.charles.project.tool.gen.domain.GenTable;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenTableMapper 测试类
 * 测试代码生成业务表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenTableMapperTest {

    @Autowired
    private GenTableMapper genTableMapper;

    private static Long testTableId;

    /**
     * 测试新增业务表
     */
    @Test
    @Order(1)
    void testInsertGenTable() {
        GenTable genTable = new GenTable();
        genTable.setTableName("test_table_" + System.currentTimeMillis());
        genTable.setTableComment("测试表");
        genTable.setClassName("TestTable");
        genTable.setPackageName("io.charles.project.test");
        genTable.setModuleName("test");
        genTable.setBusinessName("testBusiness");
        genTable.setFunctionName("测试功能");
        genTable.setFunctionAuthor("charles");
        genTable.setGenType("0");
        genTable.setGenPath("/");
        genTable.setTplCategory("crud");

        int result = genTableMapper.insertGenTable(genTable);
        assertEquals(1, result, "新增应成功");
        assertNotNull(genTable.getTableId(), "表ID不应为null");
        testTableId = genTable.getTableId();
    }

    /**
     * 测试按ID查询业务表
     */
    @Test
    @Order(2)
    void testSelectGenTableById() {
        GenTable genTable = genTableMapper.selectGenTableById(testTableId);
        assertNotNull(genTable, "业务表不应为null");
        assertEquals("测试表", genTable.getTableComment(), "表注释应匹配");
    }

    /**
     * 测试按名称查询业务表
     */
    @Test
    @Order(3)
    void testSelectGenTableByName() {
        GenTable genTable = genTableMapper.selectGenTableById(testTableId);
        GenTable result = genTableMapper.selectGenTableByName(genTable.getTableName());
        assertNotNull(result, "业务表不应为null");
    }

    /**
     * 测试查询业务表列表
     */
    @Test
    @Order(4)
    void testSelectGenTableList() {
        GenTable query = new GenTable();
        query.setTableComment("测试");
        List<GenTable> list = genTableMapper.selectGenTableList(query);
        assertFalse(list.isEmpty(), "业务表列表不应为空");
    }

    /**
     * 测试查询所有业务表
     */
    @Test
    @Order(5)
    void testSelectGenTableAll() {
        List<GenTable> list = genTableMapper.selectGenTableAll();
        assertFalse(list.isEmpty(), "业务表列表不应为空");
    }

    /**
     * 测试修改业务表
     */
    @Test
    @Order(6)
    void testUpdateGenTable() {
        GenTable genTable = genTableMapper.selectById(testTableId);
        genTable.setTableComment("更新后测试表");
        int result = genTableMapper.updateGenTable(genTable);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试批量删除业务表
     */
    @Test
    @Order(7)
    void testDeleteGenTableByIds() {
        int result = genTableMapper.deleteGenTableByIds(new Long[]{testTableId});
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(8)
    void testSelectCount() {
        Long count = genTableMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
