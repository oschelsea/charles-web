package com.xenon.project.system.mapper;

import com.xenon.common.core.domain.entity.SysDictType;
import com.xenon.system.mapper.SysDictTypeMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysDictTypeMapper 测试类
 * 测试字典类型表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysDictTypeMapperTest {

    @Autowired
    private SysDictTypeMapper sysDictTypeMapper;

    private static Long testDictId;

    /**
     * 测试新增字典类型
     */
    @Test
    @Order(1)
    void testInsertDictType() {
        SysDictType dictType = new SysDictType();
        dictType.setDictName("测试字典类型");
        dictType.setDictType("test_dict_type");
        dictType.setStatus("0");
        dictType.setRemark("测试用字典类型");

        int result = sysDictTypeMapper.insertDictType(dictType);
        assertEquals(1, result, "新增应成功");
        assertNotNull(dictType.getDictId(), "字典ID不应为null");
        testDictId = dictType.getDictId();
    }

    /**
     * 测试按ID查询字典类型
     */
    @Test
    @Order(2)
    void testSelectDictTypeById() {
        SysDictType dictType = sysDictTypeMapper.selectDictTypeById(testDictId);
        assertNotNull(dictType, "字典类型不应为null");
        assertEquals("测试字典类型", dictType.getDictName(), "字典名称应匹配");
    }

    /**
     * 测试按类型查询字典类型
     */
    @Test
    @Order(3)
    void testSelectDictTypeByType() {
        SysDictType dictType = sysDictTypeMapper.selectDictTypeByType("test_dict_type");
        assertNotNull(dictType, "字典类型不应为null");
    }

    /**
     * 测试校验字典类型唯一性
     */
    @Test
    @Order(4)
    void testCheckDictTypeUnique() {
        SysDictType dictType = sysDictTypeMapper.checkDictTypeUnique("test_dict_type");
        assertNotNull(dictType, "字典类型不应为null");
    }

    /**
     * 测试查询所有字典类型
     */
    @Test
    @Order(5)
    void testSelectDictTypeAll() {
        List<SysDictType> list = sysDictTypeMapper.selectDictTypeAll();
        assertFalse(list.isEmpty(), "字典类型列表不应为空");
    }

    /**
     * 测试查询字典类型列表
     */
    @Test
    @Order(6)
    void testSelectDictTypeList() {
        SysDictType query = new SysDictType();
        query.setDictName("测试");
        List<SysDictType> list = sysDictTypeMapper.selectDictTypeList(query);
        assertFalse(list.isEmpty(), "字典类型列表不应为空");
    }

    /**
     * 测试修改字典类型
     */
    @Test
    @Order(7)
    void testUpdateDictType() {
        SysDictType dictType = sysDictTypeMapper.selectById(testDictId);
        dictType.setDictName("更新后字典类型");
        int result = sysDictTypeMapper.updateDictType(dictType);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试删除字典类型
     */
    @Test
    @Order(8)
    void testDeleteDictTypeById() {
        int result = sysDictTypeMapper.deleteDictTypeById(testDictId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除字典类型
     */
    @Test
    @Order(9)
    void testDeleteDictTypeByIds() {
        // 先插入测试数据
        SysDictType type1 = new SysDictType();
        type1.setDictName("批量测试1");
        type1.setDictType("batch_test_type1");
        type1.setStatus("0");
        sysDictTypeMapper.insertDictType(type1);

        SysDictType type2 = new SysDictType();
        type2.setDictName("批量测试2");
        type2.setDictType("batch_test_type2");
        type2.setStatus("0");
        sysDictTypeMapper.insertDictType(type2);

        // 执行批量删除
        int result = sysDictTypeMapper.deleteDictTypeByIds(new Long[]{type1.getDictId(), type2.getDictId()});
        assertTrue(result >= 2, "批量删除应成功");
    }
}
