package io.charles.common.utils;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.github.yulichang.adapter.AdapterHelper;
import com.github.yulichang.toolkit.LogicInfoUtils;
import com.github.yulichang.toolkit.StrUtils;
import com.github.yulichang.toolkit.TableHelper;
import com.github.yulichang.toolkit.WrapperUtils;
import com.github.yulichang.toolkit.sql.SqlScriptUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.Objects;
import java.util.Optional;

/**
 * 参考 com.github.yulichang.toolkit.WrapperUtils
 */
public class MpWrapperUtils extends WrapperUtils {
    public MpWrapperUtils() {
    }

    /**
     * 构建子查询SQL
     * 在buildUnionSqlByWrapper基础上添加了distinct处理
     *
     * @param clazz   实体类
     * @param wrapper MPJLambdaWrapper
     * @return 子查询SQL
     */
    public static String buildSubSqlByWrapper(Class<?> clazz, MPJLambdaWrapper<?> wrapper) {
        TableInfo tableInfo = TableHelper.getAssert(clazz);
        String first = Optional.ofNullable(wrapper.getSqlFirst()).orElse("");
        boolean hasWhere = false;
        String entityWhere = getEntitySql(tableInfo, wrapper);
        if (StrUtils.isNotBlank(entityWhere)) {
            hasWhere = true;
        }

        String mainLogic = mainLogic(hasWhere, clazz, wrapper);
        if (StrUtils.isNotBlank(mainLogic)) {
            hasWhere = true;
        }

        String subLogic = subLogic(hasWhere, wrapper);
        if (StrUtils.isNotBlank(subLogic)) {
            hasWhere = true;
        }

        String sqlSegment = wrapper.getSqlSegment() != null && StrUtils.isNotBlank(wrapper.getSqlSegment()) ? (wrapper.isEmptyOfNormal() ? "" : (hasWhere ? " AND " : " WHERE ")) + wrapper.getSqlSegment() : "";
        String sqlComment = Optional.ofNullable(wrapper.getSqlComment()).orElse("");
        String sqlUnion = wrapper.getUnionSql();
        return String.format("%s SELECT %s %s FROM %s %s %s %s %s %s %s %s",
                first,
                wrapper.getSelectDistinct() ? "DISTINCT" : "",
                wrapper.getSqlSelect(),
                wrapper.getTableName(tableInfo.getTableName()),
                wrapper.getAlias(),
                wrapper.getFrom(),
                mainLogic,
                subLogic,
                sqlSegment,
                sqlComment,
                sqlUnion);
    }

    private static <T> String formatParam(MPJLambdaWrapper<T> wrapper, Object param) {
        String genParamName = "MPGENVAL" + wrapper.getParamNameSeq().incrementAndGet();
        String paramStr = wrapper.getParamAlias() + ".paramNameValuePairs." + genParamName;
        wrapper.getParamNameValuePairs().put(genParamName, param);
        return SqlScriptUtils.safeParam(paramStr, (String) null);
    }

    private static String getEntitySql(TableInfo tableInfo, MPJLambdaWrapper<?> wrapper) {
        Object obj = wrapper.getEntity();
        if (Objects.isNull(obj)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder("");

            for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (!AdapterHelper.getAdapter().mpjHasLogic(tableInfo) || !fieldInfo.isLogicDelete()) {
                    Object val;
                    try {
                        val = fieldInfo.getField().get(obj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    if (!Objects.isNull(val)) {
                        sb.append(" AND ").append(wrapper.getTableList().getPrefixByClass(obj.getClass())).append(".").append(fieldInfo.getColumn()).append("=").append(formatParam(wrapper, val));
                    }
                }
            }

            if (sb.length() > 0) {
                sb.delete(0, 4);
                sb.insert(0, " WHERE ");
            }

            return sb.toString();
        }
    }

    private static String mainLogic(boolean hasWhere, Class<?> clazz, MPJLambdaWrapper<?> wrapper) {
        if (!wrapper.getLogicSql()) {
            return "";
        } else {
            String info = LogicInfoUtils.getLogicInfo((Integer) null, clazz, true, wrapper.getAlias());
            if (StrUtils.isNotBlank(info)) {
                return hasWhere ? " AND " + info : " WHERE " + info.substring(4);
            } else {
                return "";
            }
        }
    }

    private static String subLogic(boolean hasWhere, MPJLambdaWrapper<?> wrapper) {
        String sql = wrapper.getSubLogicSql();
        if (StrUtils.isNotBlank(sql)) {
            return hasWhere ? sql : " WHERE " + sql.substring(4);
        } else {
            return "";
        }
    }
}
