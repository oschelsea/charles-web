package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.charles.common.utils.StringUtils;
import io.charles.project.system.domain.SysNotice;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;

/**
 * 通知公告表 数据层
 *
 * @author charles
 */
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    default SysNotice selectNoticeById(Long noticeId) {
        return selectById(noticeId);
    }

    /**
     * 查询公告列表
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    default List<SysNotice> selectNoticeList(SysNotice notice) {
        LambdaQueryWrapper<SysNotice> wrapper = Wrappers.lambdaQuery();
        if (notice != null) {
            wrapper.like(StringUtils.isNotEmpty(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
                    .eq(StringUtils.isNotEmpty(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
                    .like(StringUtils.isNotEmpty(notice.getCreateBy()), SysNotice::getCreateBy, notice.getCreateBy());
        }
        return selectList(wrapper);
    }

    /**
     * 新增公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    default int insertNotice(SysNotice notice) {
        return insert(notice);
    }

    /**
     * 修改公告
     *
     * @param notice 公告信息
     * @return 结果
     */
    default int updateNotice(SysNotice notice) {
        return updateById(notice);
    }

    /**
     * 批量删除公告
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    default int deleteNoticeById(Long noticeId) {
        return deleteById(noticeId);
    }

    /**
     * 批量删除公告信息
     *
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    default int deleteNoticeByIds(Long[] noticeIds) {
        return deleteBatchIds(Arrays.asList(noticeIds));
    }
}
