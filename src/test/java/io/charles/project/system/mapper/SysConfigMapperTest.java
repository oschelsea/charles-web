package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysConfigMapper 测试类
 * 测试参数配置表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysConfigMapperTest {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    private static Long testConfigId;

    /**
     * 测试新增参数配置
     */
    @Test
    @Order(1)
    void testInsertConfig() {
        SysConfig config = new SysConfig();
        config.setConfigName("测试配置");
        config.setConfigKey("test.config.key");
        config.setConfigValue("testValue");
        config.setConfigType("Y");
        config.setRemark("测试用配置");

        int result = sysConfigMapper.insertConfig(config);
        assertEquals(1, result, "新增应成功");
        assertNotNull(config.getConfigId(), "配置ID不应为null");
        testConfigId = config.getConfigId();
    }

    /**
     * 测试按键名查询配置
     */
    @Test
    @Order(2)
    void testCheckConfigKeyUnique() {
        SysConfig config = sysConfigMapper.checkConfigKeyUnique("test.config.key");
        assertNotNull(config, "配置不应为null");
        assertEquals("testValue", config.getConfigValue(), "配置值应匹配");
    }

    /**
     * 测试查询配置
     */
    @Test
    @Order(3)
    void testSelectConfig() {
        SysConfig query = new SysConfig();
        query.setConfigId(testConfigId);
        SysConfig config = sysConfigMapper.selectConfig(query);
        assertNotNull(config, "配置不应为null");
        assertEquals("测试配置", config.getConfigName(), "配置名称应匹配");
    }

    /**
     * 测试查询配置列表
     */
    @Test
    @Order(4)
    void testSelectConfigList() {
        SysConfig query = new SysConfig();
        query.setConfigName("测试");
        List<SysConfig> list = sysConfigMapper.selectConfigList(query);
        assertFalse(list.isEmpty(), "配置列表不应为空");
    }

    /**
     * 测试修改配置
     */
    @Test
    @Order(5)
    void testUpdateConfig() {
        SysConfig config = sysConfigMapper.selectById(testConfigId);
        config.setConfigValue("updatedValue");
        int result = sysConfigMapper.updateConfig(config);
        assertEquals(1, result, "修改应成功");

        SysConfig updated = sysConfigMapper.selectById(testConfigId);
        assertEquals("updatedValue", updated.getConfigValue(), "配置值应已更新");
    }

    /**
     * 测试删除配置
     */
    @Test
    @Order(6)
    void testDeleteConfigById() {
        int result = sysConfigMapper.deleteConfigById(testConfigId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除配置
     */
    @Test
    @Order(7)
    void testDeleteConfigByIds() {
        // 先插入测试数据
        SysConfig config1 = new SysConfig();
        config1.setConfigName("批量测试1");
        config1.setConfigKey("batch.test.key1");
        config1.setConfigValue("value1");
        config1.setConfigType("N");
        sysConfigMapper.insertConfig(config1);

        SysConfig config2 = new SysConfig();
        config2.setConfigName("批量测试2");
        config2.setConfigKey("batch.test.key2");
        config2.setConfigValue("value2");
        config2.setConfigType("N");
        sysConfigMapper.insertConfig(config2);

        // 执行批量删除
        int result = sysConfigMapper.deleteConfigByIds(new Long[]{config1.getConfigId(), config2.getConfigId()});
        assertTrue(result >= 2, "批量删除应成功");
    }
}
