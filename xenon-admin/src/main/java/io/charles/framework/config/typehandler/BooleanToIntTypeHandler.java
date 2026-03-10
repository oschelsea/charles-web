package io.charles.framework.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * boolean 与 int 互转的 TypeHandler
 * <p>
 * 解决 PostgreSQL 下 boolean 类型不能直接存入 integer 字段的问题。
 * SQLite 由于弱类型，boolean/int 可以互转，不受影响。
 * </p>
 *
 * @author charles
 */
@MappedTypes(Boolean.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class BooleanToIntTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter ? 1 : 0);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return !rs.wasNull() && value != 0;
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return !rs.wasNull() && value != 0;
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return !cs.wasNull() && value != 0;
    }
}
