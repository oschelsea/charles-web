package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.charles.common.utils.StringUtils;
import io.charles.project.system.domain.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Arrays;
import java.util.List;

import java.util.List;

/**
 * 字典表 数据层
 *
 * @author charles
 */
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    default List<SysDictData> selectDictDataList(SysDictData dictData) {
        return selectDictDataList(null, dictData);
    }

    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    default List<SysDictData> selectDictDataList(IPage<SysDictData> page, SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> wrapper = Wrappers.lambdaQuery();
        if (dictData != null) {
            wrapper.eq(StringUtils.isNotEmpty(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
                    .like(StringUtils.isNotEmpty(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel())
                    .eq(StringUtils.isNotEmpty(dictData.getStatus()), SysDictData::getStatus, dictData.getStatus());
        }
        wrapper.orderByAsc(SysDictData::getDictSort);
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    default List<SysDictData> selectDictDataByType(String dictType) {
        return selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, "0") // 0 正常
                .orderByAsc(SysDictData::getDictSort));
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    default String selectDictLabel(String dictType, String dictValue) {
        SysDictData data = selectOne(new LambdaQueryWrapper<SysDictData>()
                .select(SysDictData::getDictLabel)
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getDictValue, dictValue)
                .last("limit 1"));
        return data != null ? data.getDictLabel() : null;
    }

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    default SysDictData selectDictDataById(Long dictCode) {
        return selectById(dictCode);
    }

    /**
     * 查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据 Count
     */
    default int countDictDataByType(String dictType) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)));
    }

    /**
     * 通过字典ID删除字典数据信息
     *
     * @param dictCode 字典数据ID
     * @return 结果
     */
    default int deleteDictDataById(Long dictCode) {
        return deleteById(dictCode);
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     * @return 结果
     */
    default int deleteDictDataByIds(Long[] dictCodes) {
        return deleteBatchIds(Arrays.asList(dictCodes));
    }

    /**
     * 新增字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    default int insertDictData(SysDictData dictData) {
        return insert(dictData);
    }

    /**
     * 修改字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    default int updateDictData(SysDictData dictData) {
        return updateById(dictData);
    }

    /**
     * 同步修改字典类型
     *
     * @param oldDictType 旧字典类型
     * @param newDictType 新旧字典类型
     * @return 结果
     */
    default int updateDictDataType(String oldDictType, String newDictType) {
        return update(null, Wrappers.<SysDictData>lambdaUpdate()
                .set(SysDictData::getDictType, newDictType)
                .eq(SysDictData::getDictType, oldDictType));
    }
}
