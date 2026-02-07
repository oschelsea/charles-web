package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysNotice;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysNoticeMapper 测试类
 * 测试通知公告表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysNoticeMapperTest {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    private static Long testNoticeId;

    /**
     * 测试新增公告
     */
    @Test
    @Order(1)
    void testInsertNotice() {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("测试公告");
        notice.setNoticeType("1");
        notice.setNoticeContent("测试公告内容");
        notice.setStatus("0");
        notice.setCreateBy("admin");

        int result = sysNoticeMapper.insertNotice(notice);
        assertEquals(1, result, "新增应成功");
        assertNotNull(notice.getNoticeId(), "公告ID不应为null");
        testNoticeId = notice.getNoticeId();
    }

    /**
     * 测试按ID查询公告
     */
    @Test
    @Order(2)
    void testSelectNoticeById() {
        SysNotice notice = sysNoticeMapper.selectNoticeById(testNoticeId);
        assertNotNull(notice, "公告不应为null");
        assertEquals("测试公告", notice.getNoticeTitle(), "公告标题应匹配");
    }

    /**
     * 测试查询公告列表
     */
    @Test
    @Order(3)
    void testSelectNoticeList() {
        SysNotice query = new SysNotice();
        query.setNoticeTitle("测试");
        List<SysNotice> list = sysNoticeMapper.selectNoticeList(query);
        assertFalse(list.isEmpty(), "公告列表不应为空");
    }

    /**
     * 测试修改公告
     */
    @Test
    @Order(4)
    void testUpdateNotice() {
        SysNotice notice = sysNoticeMapper.selectById(testNoticeId);
        notice.setNoticeTitle("更新后公告");
        int result = sysNoticeMapper.updateNotice(notice);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试删除公告
     */
    @Test
    @Order(5)
    void testDeleteNoticeById() {
        int result = sysNoticeMapper.deleteNoticeById(testNoticeId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除公告
     */
    @Test
    @Order(6)
    void testDeleteNoticeByIds() {
        // 先插入测试数据
        SysNotice notice1 = new SysNotice();
        notice1.setNoticeTitle("批量测试1");
        notice1.setNoticeType("1");
        notice1.setNoticeContent("内容1");
        notice1.setStatus("0");
        sysNoticeMapper.insertNotice(notice1);

        SysNotice notice2 = new SysNotice();
        notice2.setNoticeTitle("批量测试2");
        notice2.setNoticeType("2");
        notice2.setNoticeContent("内容2");
        notice2.setStatus("0");
        sysNoticeMapper.insertNotice(notice2);

        // 执行批量删除
        int result = sysNoticeMapper.deleteNoticeByIds(new Long[]{notice1.getNoticeId(), notice2.getNoticeId()});
        assertTrue(result >= 2, "批量删除应成功");
    }
}
