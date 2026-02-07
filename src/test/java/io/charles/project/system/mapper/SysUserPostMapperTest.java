package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysUserPost;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysUserPostMapper 测试类
 * 测试重构后的 default 方法实现
 * 
 * 使用项目默认配置（dev profile）运行测试
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysUserPostMapperTest {

    @Autowired
    private SysUserPostMapper sysUserPostMapper;

    /**
     * 测试批量新增用户岗位信息
     */
    @Test
    @Order(1)
    void testBatchUserPost() {
        // 准备测试数据
        SysUserPost userPost1 = new SysUserPost();
        userPost1.setUserId(999L);
        userPost1.setPostId(1L);

        SysUserPost userPost2 = new SysUserPost();
        userPost2.setUserId(999L);
        userPost2.setPostId(2L);

        List<SysUserPost> userPostList = Arrays.asList(userPost1, userPost2);

        // 执行批量插入
        int result = sysUserPostMapper.batchUserPost(userPostList);

        // 验证结果
        assertEquals(2, result, "批量插入应返回2");
        
        // 清理测试数据
        sysUserPostMapper.deleteUserPostByUserId(999L);
    }

    /**
     * 测试通过岗位ID查询岗位使用数量
     */
    @Test
    @Order(2)
    void testCountUserPostById() {
        // 先插入测试数据
        SysUserPost userPost = new SysUserPost();
        userPost.setUserId(888L);
        userPost.setPostId(100L);
        sysUserPostMapper.insert(userPost);

        // 执行查询
        int count = sysUserPostMapper.countUserPostById(100L);

        // 验证结果
        assertTrue(count >= 1, "岗位使用数量应大于等于1");
        
        // 清理测试数据
        sysUserPostMapper.deleteUserPostByUserId(888L);
    }

    /**
     * 测试通过用户ID删除用户和岗位关联
     */
    @Test
    @Order(3)
    void testDeleteUserPostByUserId() {
        // 先插入测试数据
        SysUserPost userPost = new SysUserPost();
        userPost.setUserId(777L);
        userPost.setPostId(1L);
        sysUserPostMapper.insert(userPost);

        // 执行删除
        int result = sysUserPostMapper.deleteUserPostByUserId(777L);

        // 验证删除成功
        assertTrue(result >= 1, "删除应成功");
    }

    /**
     * 测试批量删除用户和岗位关联
     */
    @Test
    @Order(4)
    void testDeleteUserPost() {
        // 先插入测试数据
        SysUserPost userPost1 = new SysUserPost();
        userPost1.setUserId(666L);
        userPost1.setPostId(1L);
        sysUserPostMapper.insert(userPost1);

        SysUserPost userPost2 = new SysUserPost();
        userPost2.setUserId(665L);
        userPost2.setPostId(1L);
        sysUserPostMapper.insert(userPost2);

        // 执行批量删除
        int result = sysUserPostMapper.deleteUserPost(new Long[]{666L, 665L});

        // 验证删除成功
        assertTrue(result >= 2, "批量删除应成功删除至少2条记录");
    }

    /**
     * 测试 MyBatis-Plus BaseMapper 的 selectCount 方法
     */
    @Test
    @Order(5)
    void testSelectCount() {
        // 执行查询
        Long count = sysUserPostMapper.selectCount(null);

        // 验证结果（数量应该 >= 0）
        assertNotNull(count, "查询数量不应为null");
        assertTrue(count >= 0, "查询数量应大于等于0");
    }
}
