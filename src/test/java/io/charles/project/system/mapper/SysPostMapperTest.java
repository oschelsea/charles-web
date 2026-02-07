package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysPost;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysPostMapper 测试类
 * 测试岗位信息表操作
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysPostMapperTest {

    @Autowired
    private SysPostMapper sysPostMapper;

    private static Long testPostId;

    /**
     * 测试新增岗位
     */
    @Test
    @Order(1)
    void testInsertPost() {
        SysPost post = new SysPost();
        post.setPostCode("test_post");
        post.setPostName("测试岗位");
        post.setPostSort("99");
        post.setStatus("0");
        post.setRemark("测试用岗位");

        int result = sysPostMapper.insertPost(post);
        assertEquals(1, result, "新增应成功");
        assertNotNull(post.getPostId(), "岗位ID不应为null");
        testPostId = post.getPostId();
    }

    /**
     * 测试按ID查询岗位
     */
    @Test
    @Order(2)
    void testSelectPostById() {
        SysPost post = sysPostMapper.selectPostById(testPostId);
        assertNotNull(post, "岗位不应为null");
        assertEquals("测试岗位", post.getPostName(), "岗位名称应匹配");
    }

    /**
     * 测试校验岗位名称唯一性
     */
    @Test
    @Order(3)
    void testCheckPostNameUnique() {
        SysPost post = sysPostMapper.checkPostNameUnique("测试岗位");
        assertNotNull(post, "岗位不应为null");
    }

    /**
     * 测试校验岗位编码唯一性
     */
    @Test
    @Order(4)
    void testCheckPostCodeUnique() {
        SysPost post = sysPostMapper.checkPostCodeUnique("test_post");
        assertNotNull(post, "岗位不应为null");
    }

    /**
     * 测试查询所有岗位
     */
    @Test
    @Order(5)
    void testSelectPostAll() {
        List<SysPost> list = sysPostMapper.selectPostAll();
        assertFalse(list.isEmpty(), "岗位列表不应为空");
    }

    /**
     * 测试查询岗位列表
     */
    @Test
    @Order(6)
    void testSelectPostList() {
        SysPost query = new SysPost();
        query.setPostName("测试");
        List<SysPost> list = sysPostMapper.selectPostList(query);
        assertFalse(list.isEmpty(), "岗位列表不应为空");
    }

    /**
     * 测试修改岗位
     */
    @Test
    @Order(7)
    void testUpdatePost() {
        SysPost post = sysPostMapper.selectById(testPostId);
        post.setPostName("更新后岗位");
        int result = sysPostMapper.updatePost(post);
        assertEquals(1, result, "修改应成功");
    }

    /**
     * 测试删除岗位
     */
    @Test
    @Order(8)
    void testDeletePostById() {
        int result = sysPostMapper.deletePostById(testPostId);
        assertEquals(1, result, "删除应成功");
    }

    /**
     * 测试批量删除岗位
     */
    @Test
    @Order(9)
    void testDeletePostByIds() {
        // 先插入测试数据
        SysPost post1 = new SysPost();
        post1.setPostCode("batch_test1");
        post1.setPostName("批量测试1");
        post1.setPostSort("98");
        post1.setStatus("0");
        sysPostMapper.insertPost(post1);

        SysPost post2 = new SysPost();
        post2.setPostCode("batch_test2");
        post2.setPostName("批量测试2");
        post2.setPostSort("97");
        post2.setStatus("0");
        sysPostMapper.insertPost(post2);

        // 执行批量删除
        int result = sysPostMapper.deletePostByIds(new Long[]{post1.getPostId(), post2.getPostId()});
        assertTrue(result >= 2, "批量删除应成功");
    }
}
