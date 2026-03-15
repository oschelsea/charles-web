package com.xenon.project.tool.gen.mapper;

import com.xenon.project.tool.gen.domain.GenTable;
import com.xenon.project.tool.gen.domain.GenTableColumn;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenTableColumnMapper 测试类
 * 测试代码生成业务字段表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenTableColumnMapperTest {

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    @Autowired
    private GenTableMapper genTableMapper;

    private static Long testTableId;
    private static Long testColumnId;

    /**
     * 初始化测试数据 - 先创建业务表
     */
    @BeforeAll
    static void beforeAll(@Autowired GenTableMapper mapper) {
        GenTable genTable = new GenTable();
        genTable.setTableName("test_column_table_" + System.currentTimeMillis());
        genTable.setTableComment("字段测试表");
        genTable.setClassName("TestColumnTable");
        genTable.setPackageName("com.xenon.project.test");
        genTable.setModuleName("test");
        genTable.setBusinessName("testColumn");
        genTable.setFunctionName("字段测试功能");
        genTable.setFunctionAuthor("charles");
        genTable.setGenType("0");
        genTable.setGenPath("/");
        genTable.setTplCategory("crud");
        mapper.insertGenTable(genTable);
        testTableId = genTable.getTableId();
    }

    /**
     * 清理测试数据
     */
    @AfterAll
    static void afterAll(@Autowired GenTableMapper mapper) {
        if (testTableId != null) {
            mapper.deleteGenTableByIds(new Long[]{testTableId});
        }
    }

    /**
     * 测试新增业务字段
     */
    @Test
    @Order(1)
    void testInsertGenTableColumn() {
        GenTableColumn column = new GenTableColumn();
        column.setTableId(testTableId);
        column.setColumnName("test_column");
        column.setColumnComment("测试字段");
        column.setColumnType("varchar(50)");
        column.setJavaType("String");
        column.setJavaField("testColumn");
        column.setIsPk("0");
        column.setIsIncrement("0");
        column.setIsRequired("0");
        column.setIsInsert("1");
        column.setIsEdit("1");
        column.setIsList("1");
        column.setIsQuery("0");
        column.setQueryType("EQ");
        column.setHtmlType("input");
        column.setSort(1);

        int result = genTableColumnMapper.insertGenTableColumn(column);
        assertEquals(1, result, "新增应成功");
        assertNotNull(column.getColumnId(), "字段ID不应为null");
        testColumnId = column.getColumnId();
    }

    /**
     * 测试按表ID查询业务字段列表
     */
    @Test
    @Order(2)
    void testSelectGenTableColumnListByTableId() {
        List<GenTableColumn> list = genTableColumnMapper.selectGenTableColumnListByTableId(testTableId);
        assertFalse(list.isEmpty(), "业务字段列表不应为空");
    }

    /**
     * 测试修改业务字段
     */
    @Test
    @Order(3)
    void testUpdateGenTableColumn() {
        GenTableColumn column = genTableColumnMapper.selectById(testColumnId);
        column.setColumnComment("更新后测试字段");
        int result = genTableColumnMapper.updateGenTableColumn(column);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试批量删除业务字段
     */
    @Test
    @Order(4)
    void testDeleteGenTableColumnByIds() {
        int result = genTableColumnMapper.deleteGenTableColumnByIds(new Long[]{testColumnId});
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(5)
    void testSelectCount() {
        Long count = genTableColumnMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
