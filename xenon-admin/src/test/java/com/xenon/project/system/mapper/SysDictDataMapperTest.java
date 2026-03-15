package com.xenon.project.system.mapper;

import com.xenon.common.core.domain.entity.SysDictData;
import com.xenon.system.mapper.SysDictDataMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysDictDataMapper 测试类
 * 测试字典数据表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysDictDataMapperTest {

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    private static Long testDictCode;

    /**
     * 测试新增字典数据
     */
    @Test
    @Order(1)
    void testInsertDictData() {
        SysDictData dictData = new SysDictData();
        dictData.setDictSort(1L);
        dictData.setDictLabel("测试标签");
        dictData.setDictValue("test_value");
        dictData.setDictType("test_type");
        dictData.setCssClass("");
        dictData.setListClass("default");
        dictData.setStatus("0");

        int result = sysDictDataMapper.insertDictData(dictData);
        assertEquals(1, result, "新增应成功");
        assertNotNull(dictData.getDictCode(), "字典编码不应为null");
        testDictCode = dictData.getDictCode();
    }

    /**
     * 测试按ID查询字典数据
     */
    @Test
    @Order(2)
    void testSelectDictDataById() {
        SysDictData dictData = sysDictDataMapper.selectDictDataById(testDictCode);
        assertNotNull(dictData, "字典数据不应为null");
        assertEquals("测试标签", dictData.getDictLabel(), "字典标签应匹配");
    }

    /**
     * 测试按类型查询字典数据
     */
    @Test
    @Order(3)
    void testSelectDictDataByType() {
        List<SysDictData> list = sysDictDataMapper.selectDictDataByType("test_type");
        assertFalse(list.isEmpty(), "字典数据列表不应为空");
    }

    /**
     * 测试查询字典标签
     */
    @Test
    @Order(4)
    void testSelectDictLabel() {
        String label = sysDictDataMapper.selectDictLabel("test_type", "test_value");
        assertEquals("测试标签", label, "字典标签应匹配");
    }

    /**
     * 测试统计字典数据数量
     */
    @Test
    @Order(5)
    void testCountDictDataByType() {
        int count = sysDictDataMapper.countDictDataByType("test_type");
        assertTrue(count >= 1, "字典数据数量应大于等于1");
    }

    /**
     * 测试查询字典数据列表
     */
    @Test
    @Order(6)
    void testSelectDictDataList() {
        SysDictData query = new SysDictData();
        query.setDictType("test_type");
        List<SysDictData> list = sysDictDataMapper.selectDictDataList(query);
        assertFalse(list.isEmpty(), "字典数据列表不应为空");
    }

    /**
     * 测试修改字典数据
     */
    @Test
    @Order(7)
    void testUpdateDictData() {
        SysDictData dictData = sysDictDataMapper.selectById(testDictCode);
        dictData.setDictLabel("更新后标签");
        int result = sysDictDataMapper.updateDictData(dictData);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试同步修改字典类型
     */
    @Test
    @Order(8)
    void testUpdateDictDataType() {
        int result = sysDictDataMapper.updateDictDataType("test_type", "new_test_type");
        assertTrue(result >= 1, "同步修改应成功");
        // 改回来
        sysDictDataMapper.updateDictDataType("new_test_type", "test_type");
    }

    /**
     * 测试删除字典数据
     */
    @Test
    @Order(9)
    void testDeleteDictDataById() {
        int result = sysDictDataMapper.deleteDictDataById(testDictCode);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除字典数据
     */
    @Test
    @Order(10)
    void testDeleteDictDataByIds() {
        // 先插入测试数据
        SysDictData data1 = new SysDictData();
        data1.setDictLabel("批量测试1");
        data1.setDictValue("batch1");
        data1.setDictType("batch_test");
        data1.setStatus("0");
        sysDictDataMapper.insertDictData(data1);

        SysDictData data2 = new SysDictData();
        data2.setDictLabel("批量测试2");
        data2.setDictValue("batch2");
        data2.setDictType("batch_test");
        data2.setStatus("0");
        sysDictDataMapper.insertDictData(data2);

        // 执行批量删除
        int result = sysDictDataMapper.deleteDictDataByIds(new Long[]{data1.getDictCode(), data2.getDictCode()});
        assertTrue(result >= 2, "批量删除应成功");
    }
}
