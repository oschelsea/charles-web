package com.xenon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xenon.common.utils.StringUtils;
import com.xenon.common.utils.WrapperBuilder;
import com.xenon.common.core.domain.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 字典表 数据层
 *
 * @author charles
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
    /**
     * 根据条件分页查询字典类型
     *
     * @param dictType 字典类型信息
     * @return 字典类型集合信息
     */
    default List<SysDictType> selectDictTypeList(SysDictType dictType) {
        return selectDictTypeList(null, dictType);
    }

    /**
     * 根据条件分页查询字典类型
     *
     * @param dictType 字典类型信息
     * @return 字典类型集合信息
     */
    default List<SysDictType> selectDictTypeList(IPage<SysDictType> page, SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = Wrappers.lambdaQuery();
        if (dictType != null) {
            wrapper.like(StringUtils.isNotEmpty(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
                    .eq(StringUtils.isNotEmpty(dictType.getStatus()), SysDictType::getStatus, dictType.getStatus())
                    .like(StringUtils.isNotEmpty(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType());

            Map<String, Object> params = dictType.getParams();
            if (params != null) {
                WrapperBuilder.addTimeRange(wrapper, SysDictType::getCreateTime, params);
            }
        }
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 根据所有字典类型
     *
     * @return 字典类型集合信息
     */
    default List<SysDictType> selectDictTypeAll() {
        return selectList(null);
    }

    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    default SysDictType selectDictTypeById(Long dictId) {
        return selectById(dictId);
    }

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    default SysDictType selectDictTypeByType(String dictType) {
        return selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType)
                .last("limit 1"));
    }

    /**
     * 通过字典ID删除字典信息
     *
     * @param dictId 字典ID
     * @return 结果
     */
    default int deleteDictTypeById(Long dictId) {
        return deleteById(dictId);
    }

    /**
     * 批量删除字典类型信息
     *
     * @param dictIds 需要删除的字典ID
     * @return 结果
     */
    default int deleteDictTypeByIds(Long[] dictIds) {
        return deleteByIds(Arrays.asList(dictIds));
    }

    /**
     * 新增字典类型信息
     *
     * @param dictType 字典类型信息
     * @return 结果
     */
    default int insertDictType(SysDictType dictType) {
        return insert(dictType);
    }

    /**
     * 修改字典类型信息
     *
     * @param dictType 字典类型信息
     * @return 结果
     */
    default int updateDictType(SysDictType dictType) {
        return updateById(dictType);
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dictType 字典类型
     * @return 结果
     */
    default SysDictType checkDictTypeUnique(String dictType) {
        return selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType)
                .last("limit 1"));
    }
}
