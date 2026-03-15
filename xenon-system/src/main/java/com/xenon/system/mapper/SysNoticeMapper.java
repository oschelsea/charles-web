package com.xenon.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xenon.common.utils.StringUtils;
import com.xenon.system.domain.SysNotice;

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
        return selectNoticeList(null, notice);
    }

    /**
     * 查询公告列表
     *
     * @param page   分页对象
     * @param notice 公告信息
     * @return 公告集合
     */
    default List<SysNotice> selectNoticeList(IPage<SysNotice> page, SysNotice notice) {
        LambdaQueryWrapper<SysNotice> wrapper = Wrappers.lambdaQuery();
        if (notice != null) {
            wrapper.like(StringUtils.isNotEmpty(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
                    .eq(StringUtils.isNotEmpty(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
                    .like(StringUtils.isNotEmpty(notice.getCreateBy()), SysNotice::getCreateBy, notice.getCreateBy());
        }
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
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
