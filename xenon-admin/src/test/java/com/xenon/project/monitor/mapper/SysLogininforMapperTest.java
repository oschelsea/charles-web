package com.xenon.project.monitor.mapper;

import com.xenon.system.domain.SysLogininfor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysLogininforMapper 测试类
 * 测试登录日志表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysLogininforMapperTest {

    @Autowired
    private SysLogininforMapper sysLogininforMapper;

    private static Long testInfoId;

    /**
     * 测试新增登录日志
     */
    @Test
    @Order(1)
    void testInsertLogininfor() {
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName("testuser");
        logininfor.setIpaddr("127.0.0.1");
        logininfor.setLoginLocation("本机");
        logininfor.setBrowser("Chrome");
        logininfor.setOs("Windows 10");
        logininfor.setStatus("0");
        logininfor.setMsg("登录成功");

        sysLogininforMapper.insertLogininfor(logininfor);
        assertNotNull(logininfor.getInfoId(), "登录日志ID不应为null");
        testInfoId = logininfor.getInfoId();
    }

    /**
     * 测试按ID查询登录日志
     */
    @Test
    @Order(2)
    void testSelectById() {
        SysLogininfor logininfor = sysLogininforMapper.selectById(testInfoId);
        assertNotNull(logininfor, "登录日志不应为null");
        assertEquals("testuser", logininfor.getUserName(), "用户名应匹配");
    }

    /**
     * 测试查询登录日志列表
     */
    @Test
    @Order(3)
    void testSelectLogininforList() {
        SysLogininfor query = new SysLogininfor();
        query.setUserName("testuser");
        List<SysLogininfor> list = sysLogininforMapper.selectLogininforList(query);
        assertFalse(list.isEmpty(), "登录日志列表不应为空");
    }

    /**
     * 测试批量删除登录日志
     */
    @Test
    @Order(4)
    void testDeleteLogininforByIds() {
        int result = sysLogininforMapper.deleteLogininforByIds(new Long[]{testInfoId});
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试 selectCount 方法
     */
    @Test
    @Order(5)
    void testSelectCount() {
        Long count = sysLogininforMapper.selectCount(null);
        assertNotNull(count, "查询数量不应为null");
    }
}
