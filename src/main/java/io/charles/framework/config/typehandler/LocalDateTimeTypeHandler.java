package io.charles.framework.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime TypeHandler
 * Support SQLite TEXT and LocalDateTime conversion
 *
 * @author charles
 */
@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes({JdbcType.TIMESTAMP, JdbcType.VARCHAR})
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.format(FORMATTER));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseDateTime(rs.getObject(columnName));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseDateTime(rs.getObject(columnIndex));
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseDateTime(cs.getObject(columnIndex));
    }

    /**
     * Parse object to LocalDateTime
     */
    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        // Already LocalDateTime
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        // Timestamp type (MySQL, PostgreSQL native time type)
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        // String type (SQLite TEXT type)
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.isEmpty()) {
                return null;
            }
            try {
                // Standard format: yyyy-MM-dd HH:mm:ss
                return LocalDateTime.parse(strValue, FORMATTER);
            } catch (Exception e) {
                try {
                    // Date only format: yyyy-MM-dd (append 00:00:00)
                    if (strValue.length() == 10 && strValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        return LocalDateTime.parse(strValue + " 00:00:00", FORMATTER);
                    }
                    // ISO format: yyyy-MM-ddTHH:mm:ss
                    return LocalDateTime.parse(strValue.replace("T", " ").substring(0, 19), FORMATTER);
                } catch (Exception ex) {
                    // Try default parse
                    return LocalDateTime.parse(strValue);
                }
            }
        }
        throw new IllegalArgumentException("Cannot parse datetime value: " + value + ", type: " + value.getClass().getName());
    }
}
