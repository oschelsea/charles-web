package io.charles.project.tool.gen.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.charles.common.utils.StringUtils;
import io.charles.common.utils.WrapperBuilder;
import io.charles.project.tool.gen.domain.GenTable;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 业务 数据层
 *
 * @author charles
 */
public interface GenTableMapper extends BaseMapper<GenTable> {
    /**
     * 查询据库列表
     *
     * @param genTable 业务信息
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableList(IPage<GenTable> page, @Param("genTable") GenTable genTable);

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableListByNames(String[] tableNames);

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询表ID业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTable selectGenTableById(Long id);

    /**
     * 查询表名称业务信息
     *
     * @param tableName 表名称
     * @return 业务信息
     */
    GenTable selectGenTableByName(String tableName);

    /**
     * 新增业务
     *
     * @param genTable 业务信息
     * @return 结果
     */
    default int insertGenTable(GenTable genTable) {
        return insert(genTable);
    }

    /**
     * 修改业务
     *
     * @param genTable 业务信息
     * @return 结果
     */
    default int updateGenTable(GenTable genTable) {
        return updateById(genTable);
    }

    /**
     * 批量删除业务
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    default int deleteGenTableByIds(Long[] ids) {
        return delete(new LambdaQueryWrapper<GenTable>().in(GenTable::getTableId, Arrays.asList(ids)));
    }

    /**
     * 查询业务列表
     *
     * @param genTable 业务信息
     * @return 业务集合
     */
    default List<GenTable> selectGenTableList(GenTable genTable) {
        return selectGenTableList(null, genTable);
    }

    /**
     * 查询业务列表
     *
     * @param genTable 业务信息
     * @return 业务集合
     */
    default List<GenTable> selectGenTableList(IPage<GenTable> page, GenTable genTable) {
        LambdaQueryWrapper<GenTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(genTable.getTableName()), GenTable::getTableName, genTable.getTableName())
                .like(StringUtils.isNotEmpty(genTable.getTableComment()), GenTable::getTableComment, genTable.getTableComment());

        if (genTable.getParams() != null) {
            LocalDateTime beginTime = WrapperBuilder.parseDateTime(genTable.getParams().get("beginTime"));
            LocalDateTime endTime = WrapperBuilder.parseDateTime(genTable.getParams().get("endTime"));
            wrapper.ge(beginTime != null, GenTable::getCreateTime, beginTime)
                   .le(endTime != null, GenTable::getCreateTime, endTime);
        }

        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
        return selectList(wrapper);
    }
}
