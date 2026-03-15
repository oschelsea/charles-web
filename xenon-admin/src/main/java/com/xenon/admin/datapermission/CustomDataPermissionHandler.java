package com.xenon.admin.datapermission;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.xenon.admin.aspectj.DataScopeContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

/**
 * 客户数据权限处理器
 *
 * @author charles
 */
@Slf4j
public class CustomDataPermissionHandler implements DataPermissionHandler {

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        String dataScopeSql = DataScopeContextHolder.get();
        if (StringUtils.isBlank(dataScopeSql)) {
            return where;
        }

        try {
            // DataScopeAspect usually produces " AND (...)" or " OR (...)"
            // We need to parse this into an Expression
            String sqlCondition = dataScopeSql.trim();
            // Remove leading AND/OR for parsing
            if (sqlCondition.toUpperCase().startsWith("AND")) {
                sqlCondition = sqlCondition.substring(3);
            } else if (sqlCondition.toUpperCase().startsWith("OR")) {
                // OR usually implies it's appended to existing conditions, but if we are returning a standalone expression
                // to be ANDed with the main query, it should be fine.
                // However, usually DataScopeAspect produces " OR dept_id = ... OR ..." which means
                // effective SQL is: WHERE (original) AND (scope_condition)
                sqlCondition = sqlCondition.substring(2);
            }
            Expression dataScopeExpression = CCJSqlParserUtil.parseCondExpression(sqlCondition);
            if (where == null) {
                return dataScopeExpression;
            }
            return new AndExpression(where, dataScopeExpression);
        } catch (Exception e) {
            log.warn("Failed to parse data scope SQL: {}", dataScopeSql, e);
            return where;
        }
    }
}
