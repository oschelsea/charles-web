package io.charles.project.monitor.mapper;

import io.charles.project.monitor.domain.SysOperLog;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysOperLogMapper 测试类
 * 测试操作日志表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysOperLogMapperTest {

    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    private static Long testOperId;

    /**
     * 测试新增操作日志
     */
    @Test
    @Order(1)
    void testInsertOperlog() {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("测试模块");
        operLog.setBusinessType(0);
        operLog.setMethod("io.charles.test.TestController.testMethod");
        operLog.setRequestMethod("GET");
        operLog.setOperatorType(1);
        operLog.setOperName("admin");
        operLog.setOperUrl("/test");
        operLog.setOperIp("127.0.0.1");
        operLog.setOperLocation("本机");
        operLog.setStatus(0);

        sysOperLogMapper.insertOperlog(operLog);
        assertNotNull(operLog.getOperId(), "操作日志ID不应为null");
        testOperId = operLog.getOperId();
    }

    /**
     * 测试按ID查询操作日志
     */
    @Test
    @Order(2)
    void testSelectOperLogById() {
        SysOperLog operLog = sysOperLogMapper.selectOperLogById(testOperId);
        assertNotNull(operLog, "操作日志不应为null");
        assertEquals("测试模块", operLog.getTitle(), "日志标题应匹配");
    }

    /**
     * 测试查询操作日志列表
     */
    @Test
    @Order(3)
    void testSelectOperLogList() {
        SysOperLog query = new SysOperLog();
        query.setTitle("测试");
        List<SysOperLog> list = sysOperLogMapper.selectOperLogList(query);
        assertFalse(list.isEmpty(), "操作日志列表不应为空");
    }

    /**
     * 测试批量删除操作日志
     */
    @Test
    @Order(4)
    void testDeleteOperLogByIds() {
        int result = sysOperLogMapper.deleteOperLogByIds(new Long[]{testOperId});
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(5)
    void testSelectCount() {
        Long count = sysOperLogMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
