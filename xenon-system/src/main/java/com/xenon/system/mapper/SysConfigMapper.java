package com.xenon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xenon.common.utils.StringUtils;
import com.xenon.common.utils.WrapperBuilder;
import com.xenon.system.domain.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;

/**
 * 参数配置 数据层
 *
 * @author charles
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    /**
     * 查询参数配置信息
     *
     * @param config 参数配置信息
     * @return 参数配置信息
     */
    default SysConfig selectConfig(SysConfig config) {
        return selectById(config.getConfigId());
    }

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    default List<SysConfig> selectConfigList(IPage<SysConfig> page, SysConfig config) {
        LambdaQueryWrapper<SysConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotEmpty(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
                .eq(StringUtils.isNotEmpty(config.getConfigType()), SysConfig::getConfigType, config.getConfigType())
                .like(StringUtils.isNotEmpty(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey());

        if (config.getParams() != null) {
            WrapperBuilder.addTimeRange(wrapper, SysConfig::getCreateTime, config.getParams());
        }
        if (page != null) {
            return selectPage(page, wrapper).getRecords();
        }
        return selectList(wrapper);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    default SysConfig checkConfigKeyUnique(String configKey) {
        return selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("limit 1"));
    }

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    default int insertConfig(SysConfig config) {
        return insert(config);
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    default int updateConfig(SysConfig config) {
        return updateById(config);
    }

    /**
     * 删除参数配置
     *
     * @param configId 参数ID
     * @return 结果
     */
    default int deleteConfigById(Long configId) {
        return deleteById(configId);
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     * @return 结果
     */
    default int deleteConfigByIds(Long[] configIds) {
        return deleteByIds(Arrays.asList(configIds));
    }
}
