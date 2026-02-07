package io.charles.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.charles.common.utils.StringUtils;
import io.charles.project.monitor.domain.SysLogininfor;
import io.charles.project.system.domain.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    default List<SysConfig> selectConfigList(SysConfig config) {
        LambdaQueryWrapper<SysConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotEmpty(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
                .eq(StringUtils.isNotEmpty(config.getConfigType()), SysConfig::getConfigType, config.getConfigType())
                .like(StringUtils.isNotEmpty(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey());

        if (config.getParams() != null) {
            Map<String, Object> params = config.getParams();
            if (params.get("beginTime") != null && StringUtils.isNotEmpty(params.get("beginTime").toString())) {
                wrapper.ge(SysConfig::getCreateTime, params.get("beginTime"));
            }
            if (params.get("endTime") != null && StringUtils.isNotEmpty(params.get("endTime").toString())) {
                wrapper.le(SysConfig::getCreateTime, params.get("endTime"));
            }
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
        return deleteBatchIds(Arrays.asList(configIds));
    }
}
