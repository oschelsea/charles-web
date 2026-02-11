package io.charles.project.monitor.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charles.common.utils.DateUtils;
import io.charles.common.utils.StringUtils;
import io.charles.project.monitor.domain.SysOperLog;

import java.util.Arrays;
import java.util.List;

/**
 * 操作日志 数据层
 *
 * @author charles
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    default int insertOperlog(SysOperLog operLog) {
        operLog.setOperTime(DateUtils.getTime());
        return insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    default List<SysOperLog> selectOperLogList(SysOperLog operLog) {
        return selectOperLogList(null, operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    default List<SysOperLog> selectOperLogList(IPage<SysOperLog> page, SysOperLog operLog) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
                .eq(operLog.getBusinessType() != null, SysOperLog::getBusinessType, operLog.getBusinessType())
                .in(operLog.getBusinessTypes() != null && operLog.getBusinessTypes().length > 0, SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes() != null ? operLog.getBusinessTypes() : new Integer[0]))
                .eq(operLog.getStatus() != null, SysOperLog::getStatus, operLog.getStatus())
                .like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName());

        if (operLog.getParams() != null) {
            Object beginTime = operLog.getParams().get("beginTime");
            Object endTime = operLog.getParams().get("endTime");
            if (beginTime != null && !beginTime.toString().isEmpty()) {
                wrapper.ge(SysOperLog::getOperTime, beginTime);
            }
            if (endTime != null && !endTime.toString().isEmpty()) {
                wrapper.le(SysOperLog::getOperTime, endTime);
            }
        }
        wrapper.orderByDesc(SysOperLog::getOperId);
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    default int deleteOperLogByIds(Long[] operIds) {
        return deleteBatchIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    default SysOperLog selectOperLogById(Long operId) {
        return selectById(operId);
    }

    /**
     * 清空操作日志
     */
    public void cleanOperLog();
}
