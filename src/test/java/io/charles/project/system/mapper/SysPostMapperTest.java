package io.charles.project.system.mapper;

import io.charles.project.system.domain.SysPost;
import io.charles.project.system.domain.SysUser;
import io.charles.project.system.domain.SysUserPost;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysPostMapper 测试类
 *
 * @author charles
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SysPostMapperTest {

    @Autowired
    private SysPostMapper sysPostMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserPostMapper sysUserPostMapper;

    @Test
    @Order(1)
    void testSelectPostListByUserId() {
        // 1. Create a user
        SysUser user = new SysUser();
        user.setUserName("post_user_" + System.currentTimeMillis());
        user.setNickName("Post User");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        // 2. Create a post
        SysPost post = new SysPost();
        post.setPostCode("post_code_" + System.currentTimeMillis());
        post.setPostName("Post Name");
        post.setPostSort("1");
        post.setStatus("0");
        sysPostMapper.insertPost(post);
        Long postId = post.getPostId();

        // 3. Associate user and post
        SysUserPost up = new SysUserPost();
        up.setUserId(userId);
        up.setPostId(postId);
        sysUserPostMapper.insert(up);

        // 4. Test selectPostListByUserId
        List<Integer> postIds = sysPostMapper.selectPostListByUserId(userId);
        assertFalse(postIds.isEmpty());
        assertTrue(postIds.contains(postId.intValue()));
        
        // Clean up handled by test transaction rollback if configured, or manual cleanup (skipping for simplicity in this context as per user rule 3)
    }

    @Test
    @Order(2)
    void testSelectPostsByUserName() {
        // Reuse setup logic or create new data
        SysUser user = new SysUser();
        String userName = "post_user_name_" + System.currentTimeMillis();
        user.setUserName(userName);
        user.setNickName("Post User Name");
        sysUserMapper.insertUser(user);
        Long userId = user.getUserId();

        SysPost post = new SysPost();
        post.setPostCode("post_code_name_" + System.currentTimeMillis());
        post.setPostName("Post Name for Name Test");
        post.setPostSort("2");
        post.setStatus("0");
        sysPostMapper.insertPost(post);
        Long postId = post.getPostId();

        SysUserPost up = new SysUserPost();
        up.setUserId(userId);
        up.setPostId(postId);
        sysUserPostMapper.insert(up);

        List<SysPost> posts = sysPostMapper.selectPostsByUserName(userName);
        assertFalse(posts.isEmpty());
        assertEquals(postId, posts.get(0).getPostId());
        assertEquals(post.getPostName(), posts.get(0).getPostName());
    }

    private static Long testPostId;

    @Test
    @Order(3)
    void testInsertPost() {
        SysPost post = new SysPost();
        post.setPostCode("test_code_" + System.currentTimeMillis());
        post.setPostName("测试岗位");
        post.setPostSort("99");
        post.setStatus("0");
        int result = sysPostMapper.insertPost(post);
        assertEquals(1, result);
        assertNotNull(post.getPostId());
        testPostId = post.getPostId();
    }

    @Test
    @Order(4)
    void testSelectPostById() {
        SysPost post = sysPostMapper.selectPostById(testPostId);
        assertNotNull(post);
        assertEquals("测试岗位", post.getPostName());
    }

    @Test
    @Order(5)
    void testSelectPostList() {
        SysPost query = new SysPost();
        query.setPostName("测试");
        List<SysPost> list = sysPostMapper.selectPostList(query);
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(6)
    void testSelectPostAll() {
        List<SysPost> list = sysPostMapper.selectPostAll();
        assertFalse(list.isEmpty());
    }

    @Test
    @Order(7)
    void testCheckPostNameUnique() {
        SysPost post = sysPostMapper.checkPostNameUnique("测试岗位");
        assertNotNull(post);
        assertEquals(testPostId, post.getPostId());
    }

    @Test
    @Order(8)
    void testCheckPostCodeUnique() {
        SysPost existing = sysPostMapper.selectById(testPostId);
        SysPost post = sysPostMapper.checkPostCodeUnique(existing.getPostCode());
        assertNotNull(post);
        assertEquals(testPostId, post.getPostId());
    }

    @Test
    @Order(9)
    void testUpdatePost() {
        SysPost post = sysPostMapper.selectById(testPostId);
        post.setPostName("更新后岗位");
        int result = sysPostMapper.updatePost(post);
        assertEquals(1, result);
        SysPost updated = sysPostMapper.selectById(testPostId);
        assertEquals("更新后岗位", updated.getPostName());
    }

    @Test
    @Order(10)
    void testDeletePostById() {
        int result = sysPostMapper.deletePostById(testPostId);
        assertEquals(1, result);
        assertNull(sysPostMapper.selectById(testPostId));
    }

    @Test
    @Order(11)
    void testDeletePostByIds() {
        SysPost p1 = new SysPost();
        p1.setPostCode("batch1_" + System.currentTimeMillis());
        p1.setPostName("批量1");
        p1.setPostSort("1");
        p1.setStatus("0");
        sysPostMapper.insertPost(p1);

        SysPost p2 = new SysPost();
        p2.setPostCode("batch2_" + System.currentTimeMillis());
        p2.setPostName("批量2");
        p2.setPostSort("2");
        p2.setStatus("0");
        sysPostMapper.insertPost(p2);

        int result = sysPostMapper.deletePostByIds(new Long[]{p1.getPostId(), p2.getPostId()});
        assertEquals(2, result);
    }
}
