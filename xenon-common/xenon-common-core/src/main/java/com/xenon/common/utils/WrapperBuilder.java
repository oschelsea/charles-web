package com.xenon.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * MyBatis-Plus LambdaQueryWrapper 构建工具类
 *
 * @author charles
 */
public class WrapperBuilder {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 添加时间范围查询条件
     *
     * @param wrapper 查询包装器
     * @param column 时间字段
     * @param params 包含 beginTime、endTime 的参数 Map
     * @param <T> 实体类型
     */
    public static <T> void addTimeRange(LambdaQueryWrapper<T> wrapper, SFunction<T, LocalDateTime> column, Map<String, Object> params) {
        if (params == null) {
            return;
        }

        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;

        // 尝试获取时间范围参数
        Object beginObj = params.get("beginTime");
        Object endObj = params.get("endTime");

        if (beginObj == null) {
            // 尝试从嵌套 params 对象获取
            Object nestedParams = params.get("params");
            if (nestedParams instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) nestedParams;
                beginObj = nested.get("beginTime");
                endObj = nested.get("endTime");
            }
        }

        // 解析开始时间
        beginTime = parseDateTime(beginObj);
        // 解析结束时间
        endTime = parseDateTime(endObj);

        // 添加时间范围条件
        wrapper.ge(beginTime != null, column, beginTime)
               .le(endTime != null, column, endTime);
    }

    /**
     * 将对象解析为 LocalDateTime，兼容 String 和 LocalDateTime 类型
     *
     * @param value 待解析的值（String 或 LocalDateTime）
     * @return 解析后的 LocalDateTime，解析失败返回 null
     */
    public static LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof String) {
            return parseDateTimeStr((String) value);
        }
        return null;
    }

    /**
     * 解析日期时间字符串
     */
    private static LocalDateTime parseDateTimeStr(String dateTimeStr) {
        if (StringUtils.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            // yyyy-MM-dd HH:mm:ss 格式
            if (dateTimeStr.length() == 19) {
                return LocalDateTime.parse(dateTimeStr, FORMATTER);
            }
            // yyyy-MM-dd 格式
            if (dateTimeStr.length() == 10) {
                return java.time.LocalDate.parse(dateTimeStr).atStartOfDay();
            }
        } catch (Exception e) {
            // 解析失败返回 null
        }
        return null;
    }
}

