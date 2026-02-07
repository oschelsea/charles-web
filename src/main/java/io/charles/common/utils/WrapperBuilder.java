package io.charles.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MyBatis-Plus LambdaQueryWrapper Builder Utility
 *
 * @author charles
 */
public class WrapperBuilder {

    /**
     * Add time range query condition
     * 
     * @param wrapper query wrapper
     * @param column time field
     * @param params Map containing beginTime, endTime or params.beginTime, params.endTime
     * @param <T> entity type
     */
    public static <T> void addTimeRange(LambdaQueryWrapper<T> wrapper, SFunction<T, LocalDateTime> column, Map<String, Object> params) {
        if (params == null) {
            return;
        }
        
        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;
        
        // Try to get time range params
        Object beginObj = params.get("beginTime");
        Object endObj = params.get("endTime");
        
        if (beginObj == null) {
            // Try to get from nested params object
            Object nestedParams = params.get("params");
            if (nestedParams instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) nestedParams;
                beginObj = nested.get("beginTime");
                endObj = nested.get("endTime");
            }
        }
        
        // Parse begin time
        if (beginObj != null) {
            if (beginObj instanceof LocalDateTime) {
                beginTime = (LocalDateTime) beginObj;
            } else if (beginObj instanceof String) {
                beginTime = parseDateTime((String) beginObj);
            }
        }
        
        // Parse end time
        if (endObj != null) {
            if (endObj instanceof LocalDateTime) {
                endTime = (LocalDateTime) endObj;
            } else if (endObj instanceof String) {
                endTime = parseDateTime((String) endObj);
            }
        }
        
        // Add time range condition
        wrapper.ge(beginTime != null, column, beginTime)
               .le(endTime != null, column, endTime);
    }
    
    /**
     * Parse datetime string
     */
    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtils.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
            // Try yyyy-MM-dd HH:mm:ss format
            if (dateTimeStr.length() == 19) {
                return LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            // Try yyyy-MM-dd format
            if (dateTimeStr.length() == 10) {
                return java.time.LocalDate.parse(dateTimeStr).atStartOfDay();
            }
        } catch (Exception e) {
            // Parse failed, return null
        }
        return null;
    }
}
