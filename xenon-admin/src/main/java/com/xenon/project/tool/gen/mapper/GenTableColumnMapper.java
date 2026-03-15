package com.xenon.project.tool.gen.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xenon.project.tool.gen.domain.GenTableColumn;

import java.util.Arrays;
import java.util.List;

/**
 * 业务字段 数据层
 *
 * @author charles
 */
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {
    /**
     * 根据表名称查询列信息
     *
     * @param tableName 表名称
     * @return 列信息
     */
    public List<GenTableColumn> selectDbTableColumnsByName(String tableName);

    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    default List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return selectList(new LambdaQueryWrapper<GenTableColumn>()
                .eq(GenTableColumn::getTableId, tableId)
                .orderByAsc(GenTableColumn::getSort));
    }

    /**
     * 新增业务字段
     *
     * @param genTableColumn 业务字段信息
     * @return 结果
     */
    default int insertGenTableColumn(GenTableColumn genTableColumn) {
        return insert(genTableColumn);
    }

    /**
     * 修改业务字段
     *
     * @param genTableColumn 业务字段信息
     * @return 结果
     */
    default int updateGenTableColumn(GenTableColumn genTableColumn) {
        return updateById(genTableColumn);
    }

    /**
     * 删除业务字段
     *
     * @param genTableColumns 列数据
     * @return 结果
     */
    default int deleteGenTableColumns(List<GenTableColumn> genTableColumns) {
        for (GenTableColumn column : genTableColumns) {
            deleteById(column.getColumnId());
        }
        return genTableColumns.size();
    }

    /**
     * 批量删除业务字段
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    default int deleteGenTableColumnByIds(Long[] ids) {
        return deleteBatchIds(Arrays.asList(ids));
    }
}
