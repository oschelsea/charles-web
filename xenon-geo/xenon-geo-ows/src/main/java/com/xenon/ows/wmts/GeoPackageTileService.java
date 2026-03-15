package com.xenon.ows.wmts;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GeoPackage 瓦片服务。
 * 用于读取符合 OGC GeoPackage 标准的瓦片数据。
 * 使用 HikariCP 连接池管理数据库连接，提高高并发场景下的性能。
 */
@Slf4j
@Service
public class GeoPackageTileService {

    /**
     * 配置信息缓存
     */
    private final Map<String, GpkgTileConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 每个 GeoPackage 文件对应一个连接池
     */
    private final Map<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 连接池最大连接数
     */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * 连接池最小空闲连接数
     */
    private static final int MIN_IDLE = 2;

    /**
     * 连接最大生命周期（毫秒）
     */
    private static final long MAX_LIFETIME = 1800000;

    /**
     * 空闲连接超时时间（毫秒）
     */
    private static final long IDLE_TIMEOUT = 600000;

    /**
     * 获取指定 GeoPackage 文件的数据源（连接池）
     *
     * @param gpkgPath GeoPackage 文件路径
     * @return 数据源，如果文件不存在返回 null
     */
    private HikariDataSource getDataSource(String gpkgPath) {
        return dataSourceCache.computeIfAbsent(gpkgPath, path -> {
            File file = new File(path);
            if (!file.exists()) {
                log.warn("GeoPackage 文件不存在: {}", path);
                return null;
            }

            HikariConfig config = new HikariConfig();
            // SQLite 只读模式通过 URL 参数设置，避免与 HikariCP 的兼容性问题
            config.setJdbcUrl("jdbc:sqlite:" + path);
            config.setPoolName("gpkg-" + file.getName());
            config.setMaximumPoolSize(MAX_POOL_SIZE);
            config.setMinimumIdle(MIN_IDLE);
            config.setMaxLifetime(MAX_LIFETIME);
            config.setIdleTimeout(IDLE_TIMEOUT);

            log.info("创建 GeoPackage 连接池: {}", path);
            return new HikariDataSource(config);
        });
    }

    /**
     * 从连接池获取连接
     *
     * @param gpkgPath GeoPackage 文件路径
     * @return 数据库连接，如果获取失败返回 null
     */
    private Connection getConnection(String gpkgPath) throws SQLException {
        HikariDataSource ds = getDataSource(gpkgPath);
        if (ds == null) {
            return null;
        }
        return ds.getConnection();
    }

    /**
     * GeoPackage 瓦片配置信息
     */
    public static class GpkgTileConfig {
        /**
         * 瓦片表名
         */
        public String tableName;
        /**
         * 坐标系 (如 EPSG:4326)
         */
        public String srs;
        /**
         * 坐标系 SRID
         */
        public int srsId;
        /**
         * 范围 [minX, minY, maxX, maxY]
         */
        public double[] bounds;
        /**
         * 最小缩放级别
         */
        public int minZoom;
        /**
         * 最大缩放级别
         */
        public int maxZoom;
        /**
         * 瓦片宽度
         */
        public int tileWidth = 256;
        /**
         * 瓦片高度
         */
        public int tileHeight = 256;
        /**
         * 瓦片矩阵信息列表
         */
        public List<TileMatrixInfo> matrices = new ArrayList<>();
    }

    /**
     * 瓦片矩阵信息
     */
    public static class TileMatrixInfo {
        public int zoomLevel;
        public int matrixWidth;
        public int matrixHeight;
        public int tileWidth;
        public int tileHeight;
        public double pixelXSize;
        public double pixelYSize;
    }

    /**
     * 获取 GeoPackage 中所有瓦片表名称
     *
     * @param gpkgPath GeoPackage 文件路径
     * @return 瓦片表名称列表
     */
    public List<String> getTileTables(String gpkgPath) {
        List<String> tables = new ArrayList<>();

        try (Connection conn = getConnection(gpkgPath)) {
            if (conn == null) {
                return tables;
            }
            // 从 gpkg_contents 表中查询瓦片类型的表
            String sql = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tables.add(rs.getString("table_name"));
                }
            }
        } catch (SQLException e) {
            log.error("读取 GeoPackage 瓦片表失败: {}", gpkgPath, e);
        }

        return tables;
    }

    /**
     * 获取指定瓦片表的配置信息
     *
     * @param gpkgPath  GeoPackage 文件路径
     * @param tableName 瓦片表名，如果为 null 则使用第一个瓦片表
     * @return 瓦片配置信息
     */
    public GpkgTileConfig getConfig(String gpkgPath, String tableName) {
        String cacheKey = gpkgPath + ":" + (tableName != null ? tableName : "");
        if (configCache.containsKey(cacheKey)) {
            return configCache.get(cacheKey);
        }

        if (!new File(gpkgPath).exists()) {
            log.warn("GeoPackage 文件不存在: {}", gpkgPath);
            return null;
        }

        GpkgTileConfig config = new GpkgTileConfig();

        try (Connection conn = getConnection(gpkgPath)) {
            if (conn == null) {
                return null;
            }

            // 如果未指定表名，获取第一个瓦片表
            if (tableName == null || tableName.isEmpty()) {
                List<String> tables = getTileTables(gpkgPath);
                if (tables.isEmpty()) {
                    log.warn("GeoPackage 中没有瓦片表: {}", gpkgPath);
                    return null;
                }
                tableName = tables.get(0);
            }
            config.tableName = tableName;

            // 从 gpkg_contents 获取范围和 SRS
            String contentsSql = "SELECT min_x, min_y, max_x, max_y, srs_id FROM gpkg_contents WHERE table_name = ?";
            try (PreparedStatement ps = conn.prepareStatement(contentsSql)) {
                ps.setString(1, tableName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        config.bounds = new double[]{
                                rs.getDouble("min_x"),
                                rs.getDouble("min_y"),
                                rs.getDouble("max_x"),
                                rs.getDouble("max_y")
                        };
                        config.srsId = rs.getInt("srs_id");
                    }
                }
            }

            // 从 gpkg_spatial_ref_sys 获取坐标系定义
            String srsSql = "SELECT organization, organization_coordsys_id FROM gpkg_spatial_ref_sys WHERE srs_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(srsSql)) {
                ps.setInt(1, config.srsId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String org = rs.getString("organization");
                        int coordSysId = rs.getInt("organization_coordsys_id");
                        config.srs = org.toUpperCase() + ":" + coordSysId;
                    }
                }
            }

            // 从 gpkg_tile_matrix 获取瓦片矩阵信息
            String matrixSql = "SELECT zoom_level, matrix_width, matrix_height, tile_width, tile_height, " +
                    "pixel_x_size, pixel_y_size FROM gpkg_tile_matrix WHERE table_name = ? ORDER BY zoom_level";
            try (PreparedStatement ps = conn.prepareStatement(matrixSql)) {
                ps.setString(1, tableName);
                try (ResultSet rs = ps.executeQuery()) {
                    int minZoom = Integer.MAX_VALUE;
                    int maxZoom = Integer.MIN_VALUE;

                    while (rs.next()) {
                        TileMatrixInfo matrix = new TileMatrixInfo();
                        matrix.zoomLevel = rs.getInt("zoom_level");
                        matrix.matrixWidth = rs.getInt("matrix_width");
                        matrix.matrixHeight = rs.getInt("matrix_height");
                        matrix.tileWidth = rs.getInt("tile_width");
                        matrix.tileHeight = rs.getInt("tile_height");
                        matrix.pixelXSize = rs.getDouble("pixel_x_size");
                        matrix.pixelYSize = rs.getDouble("pixel_y_size");

                        config.matrices.add(matrix);

                        if (matrix.zoomLevel < minZoom) {
                            minZoom = matrix.zoomLevel;
                        }
                        if (matrix.zoomLevel > maxZoom) {
                            maxZoom = matrix.zoomLevel;
                        }

                        // 使用第一个矩阵的瓦片尺寸作为默认值
                        if (config.matrices.size() == 1) {
                            config.tileWidth = matrix.tileWidth;
                            config.tileHeight = matrix.tileHeight;
                        }
                    }

                    if (minZoom != Integer.MAX_VALUE) {
                        config.minZoom = minZoom;
                        config.maxZoom = maxZoom;
                    }
                }
            }

            configCache.put(cacheKey, config);
            return config;

        } catch (SQLException e) {
            log.error("解析 GeoPackage 配置失败: {}", gpkgPath, e);
            return null;
        }
    }

    /**
     * 读取瓦片数据
     *
     * @param gpkgPath  GeoPackage 文件路径
     * @param tableName 瓦片表名
     * @param z         缩放级别
     * @param row       行号 (WMTS 风格，从上到下)
     * @param col       列号
     * @return 瓦片图像数据 (PNG/JPEG)
     */
    public byte[] getTile(String gpkgPath, String tableName, int z, int row, int col) {
        try (Connection conn = getConnection(gpkgPath)) {
            if (conn == null) {
                return null;
            }

            // GeoPackage 使用 TMS 风格的行编号（Y 轴从下往上）
            // 需要根据实际情况决定是否翻转
            // 标准 GeoPackage: tile_row 从下往上编号
            // WMTS: row 从上往下编号
            // 因此需要翻转: gpkgRow = matrixHeight - 1 - row

            // 获取配置以确定是否需要翻转
            GpkgTileConfig config = getConfig(gpkgPath, tableName);
            int gpkgRow = row;

            // 检查是否需要翻转 Y 轴
            // 大多数 GeoPackage 使用 TMS 风格（Y 轴从下往上）
            // 而 WMTS 使用从上往下的行编号
            if (config != null) {
                // 查找对应 zoom 级别的矩阵高度
                for (TileMatrixInfo matrix : config.matrices) {
                    if (matrix.zoomLevel == z) {
                        // TMS 到 WMTS 的转换: gpkgRow = matrixHeight - 1 - row
                        gpkgRow = matrix.matrixHeight - 1 - row;
                        break;
                    }
                }
            }

            String sql = "SELECT tile_data FROM " + sanitizeTableName(tableName) +
                    " WHERE zoom_level = ? AND tile_row = ? AND tile_column = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, z);
                ps.setInt(2, gpkgRow);
                ps.setInt(3, col);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBytes("tile_data");
                    }
                }
            }
        } catch (SQLException e) {
            log.error("读取 GeoPackage 瓦片失败: {} z={}, row={}, col={}", gpkgPath, z, row, col, e);
        }

        return null;
    }

    /**
     * 清理表名，防止 SQL 注入
     */
    private String sanitizeTableName(String tableName) {
        // 只允许字母、数字和下划线
        return tableName.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * 清除配置缓存
     */
    public void clearCache() {
        configCache.clear();
    }

    /**
     * 清除指定文件的配置缓存
     */
    public void clearCache(String gpkgPath) {
        configCache.keySet().removeIf(key -> key.startsWith(gpkgPath + ":"));
    }

    /**
     * 关闭指定文件的连接池
     *
     * @param gpkgPath GeoPackage 文件路径
     */
    public void closeDataSource(String gpkgPath) {
        HikariDataSource ds = dataSourceCache.remove(gpkgPath);
        if (ds != null && !ds.isClosed()) {
            ds.close();
            log.info("关闭 GeoPackage 连接池: {}", gpkgPath);
        }
        clearCache(gpkgPath);
    }

    /**
     * 服务销毁时关闭所有连接池
     */
    @PreDestroy
    public void destroy() {
        log.info("关闭所有 GeoPackage 连接池...");
        dataSourceCache.forEach((path, ds) -> {
            if (ds != null && !ds.isClosed()) {
                ds.close();
                log.debug("关闭连接池: {}", path);
            }
        });
        dataSourceCache.clear();
        configCache.clear();
    }
}
