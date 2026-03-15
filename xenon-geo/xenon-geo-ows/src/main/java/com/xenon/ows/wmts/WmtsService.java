package com.xenon.ows.wmts;

import com.xenon.core.entity.Layer;
import com.xenon.core.service.LayerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.GeneralBounds;
import org.geotools.referencing.CRS;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * WMTS (Web Map Tile Service) implementation.
 * Supports WMTS 1.0.0 specification with RESTful and KVP bindings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WmtsService {

    private final LayerService layerService;
    private final ArcGISCacheService arcGISCacheService;
    private final GeoPackageTileService geoPackageTileService;
    private final com.xenon.core.service.DataStoreService dataStoreService;

    /**
     * Get WMTS Capabilities document.
     */
    /**
     * Get WMTS Capabilities document.
     */
    public String getCapabilities(String baseUrl, String layerName) {
        List<Layer> layers;
        if (layerName != null && !layerName.isEmpty()) {
            Layer layer = getLayer(layerName);
            layers = layer != null ? List.of(layer) : List.of();
        } else {
            layers = layerService.findAdvertised();
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Capabilities xmlns=\"http://www.opengis.net/wmts/1.0\" ");
        xml.append("xmlns:ows=\"http://www.opengis.net/ows/1.1\" ");
        xml.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
        xml.append("xmlns:gml=\"http://www.opengis.net/gml\" ");
        xml.append("version=\"1.0.0\">\n");

        // Service Identification
        xml.append("  <ows:ServiceIdentification>\n");
        xml.append("    <ows:Title>Xenon WMTS</ows:Title>\n");
        xml.append("    <ows:Abstract>Web Map Tile Service provided by Xenon</ows:Abstract>\n");
        xml.append("    <ows:ServiceType>OGC WMTS</ows:ServiceType>\n");
        xml.append("    <ows:ServiceTypeVersion>1.0.0</ows:ServiceTypeVersion>\n");
        xml.append("  </ows:ServiceIdentification>\n");

        // Service Provider
        xml.append("  <ows:ServiceProvider>\n");
        xml.append("    <ows:ProviderName>Xenon</ows:ProviderName>\n");
        xml.append("  </ows:ServiceProvider>\n");

        // Operations Metadata
        xml.append("  <ows:OperationsMetadata>\n");
        appendOperation(xml, "GetCapabilities", baseUrl);
        appendOperation(xml, "GetTile", baseUrl);
        xml.append("  </ows:OperationsMetadata>\n");

        // Contents
        xml.append("  <Contents>\n");

        // Add layers
        for (Layer layer : layers) {
            appendLayerXml(xml, layer, baseUrl);
        }

        // Tile Matrix Sets
        for (Layer layer : layers) {
            if (layer.getType() == com.xenon.core.enums.LayerType.ARCGIS_CACHE) {
                appendArcGISRefMatrixSet(xml, layer);
            } else if (layer.getType() == com.xenon.core.enums.LayerType.GEOPACKAGE_TILES) {
                appendGeoPackageMatrixSet(xml, layer);
            }
        }

        //appendTileMatrixSet(xml, "EPSG:4326", "urn:ogc:def:crs:EPSG::4326", -180, 90);
        //appendTileMatrixSet(xml, "EPSG:3857", "urn:ogc:def:crs:EPSG::3857", -20037508.34, 20037508.34);

        xml.append("  </Contents>\n");
        xml.append("</Capabilities>");

        return xml.toString();
    }

    private Layer getLayer(String layerName) {
        Layer layer;
        if (layerName.contains(":")) {
            String[] parts = layerName.split(":");
            String workspaceName = parts[0];
            String simpleName = parts[1];
            layer = layerService.findByNameAndWorkspace(simpleName, workspaceName);
        } else {
            throw new IllegalArgumentException("Layer name must be qualified with workspace (workspace:layerName)");
        }
        return layer;
    }

    /**
     * Get a tile image.
     */
    public byte[] getTile(String layerName, String style, String tileMatrixSet,
                          int tileMatrix, int tileRow, int tileCol, String format) throws IOException {
        log.debug("GetTile: layer={}, z={}, row={}, col={}", layerName, tileMatrix, tileRow, tileCol);

        // Verify layer exists
        Layer layerEntity = null;
        try {
            layerEntity = getLayer(layerName);
        } catch (Exception e) {
            log.warn("Layer not found: {}", layerName);
            return new byte[0];
        }

        if (layerEntity != null && layerEntity.getType() == com.xenon.core.enums.LayerType.ARCGIS_CACHE) {
            // Fetch datastore path
            if (layerEntity.getDatastoreId() != null) {
                com.xenon.core.entity.DataStore ds = dataStoreService.findById(layerEntity.getDatastoreId());
                if (ds != null && ds.getConnectionParams() != null) {
                    String path = (String) ds.getConnectionParams().get("path");
                    if (path != null) {
                        try {
                            byte[] tile = arcGISCacheService.getTile(path, tileMatrix, tileRow, tileCol);
                            if (tile != null) {
                                return tile;
                            }
                        } catch (Exception e) {
                            log.error("Failed to read ArcGIS cache tile", e);
                        }
                    }
                }
            }
            // If failed or not found, return empty or default
            // Or return transparent 404 image
            return new byte[0];
        }

        // 处理 GeoPackage 瓦片图层
        if (layerEntity != null && layerEntity.getType() == com.xenon.core.enums.LayerType.GEOPACKAGE_TILES) {
            if (layerEntity.getDatastoreId() != null) {
                com.xenon.core.entity.DataStore ds = dataStoreService.findById(layerEntity.getDatastoreId());
                if (ds != null && ds.getConnectionParams() != null) {
                    String gpkgPath = (String) ds.getConnectionParams().get("database");
                    // 瓦片表名可以从连接参数或图层名称获取
                    String tableName = (String) ds.getConnectionParams().get("table");
                    if (tableName == null || tableName.isEmpty()) {
                        // 默认使用图层名称作为表名
                        tableName = layerEntity.getName();
                    }
                    if (gpkgPath != null) {
                        try {
                            byte[] tile = geoPackageTileService.getTile(gpkgPath, tableName, tileMatrix, tileRow, tileCol);
                            if (tile != null) {
                                return tile;
                            }
                        } catch (Exception e) {
                            log.error("Failed to read GeoPackage tile", e);
                        }
                    }
                }
            }
            return new byte[0];
        }

        // Default implementation (Grid drawing)
        // ... (rest of the file)

        // Create 256x256 tile
        int tileSize = 256;
        BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Transparent background
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, tileSize, tileSize);
            g2d.setComposite(AlphaComposite.SrcOver);

            // Draw tile grid
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.drawRect(0, 0, tileSize - 1, tileSize - 1);

            // Draw tile info
            g2d.setColor(new Color(100, 100, 100, 150));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String info = String.format("Z:%d R:%d C:%d", tileMatrix, tileRow, tileCol);
            g2d.drawString(info, 5, 15);

            String name = layerEntity != null ? layerEntity.getName() : (layerName != null ? layerName : "no layer");
            g2d.drawString(name, 5, 30);

            if (layerEntity != null && layerEntity.getType() != null) {
                g2d.drawString("Type: " + layerEntity.getType().name(), 5, 45);
            }

        } finally {
            g2d.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = format != null && format.contains("jpeg") ? "jpg" : "png";
        ImageIO.write(image, formatName, baos);

        return baos.toByteArray();
    }

    /**
     * Get list of available WMTS layers.
     */
    public List<Layer> getWmtsLayers() {
        return layerService.findAdvertised();
    }

    /**
     * Build base URL from request.
     */
    public String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) ||
                ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }

    private void appendOperation(StringBuilder xml, String name, String baseUrl) {
        xml.append("    <ows:Operation name=\"").append(name).append("\">\n");
        xml.append("      <ows:DCP>\n");
        xml.append("        <ows:HTTP>\n");
        xml.append("          <ows:Get xlink:href=\"").append(baseUrl).append("/wmts?\">\n");
        xml.append("            <ows:Constraint name=\"GetEncoding\">\n");
        xml.append("              <ows:AllowedValues><ows:Value>KVP</ows:Value></ows:AllowedValues>\n");
        xml.append("            </ows:Constraint>\n");
        xml.append("          </ows:Get>\n");
        xml.append("        </ows:HTTP>\n");
        xml.append("      </ows:DCP>\n");
        xml.append("    </ows:Operation>\n");
    }

    private void appendLayerXml(StringBuilder xml, Layer layer, String baseUrl) {
        xml.append("    <Layer>\n");
        xml.append("      <ows:Title>").append(escapeXml(layer.getTitle() != null ? layer.getTitle() : layer.getName())).append("</ows:Title>\n");
        if (layer.getDescription() != null) {
            xml.append("      <ows:Abstract>").append(escapeXml(layer.getDescription())).append("</ows:Abstract>\n");
        }
        xml.append("      <ows:Identifier>").append(escapeXml(layer.getName())).append("</ows:Identifier>\n");

        // BoundingBox - default to world extent if not available
        double minx = -180, miny = -90, maxx = 180, maxy = 90;

        // Ensure accurate extent for ArcGIS Cache
        if (layer.getType() == com.xenon.core.enums.LayerType.ARCGIS_CACHE && layer.getDatastoreId() != null) {
            com.xenon.core.entity.DataStore ds = dataStoreService.findById(layer.getDatastoreId());
            if (ds != null && ds.getConnectionParams() != null) {
                String path = (String) ds.getConnectionParams().get("path");
                if (path != null) {
                    ArcGISCacheService.CacheConfig config = arcGISCacheService.getCacheConfig(path);
                    if (config != null && config.fullExtent != null && config.crs != null) {
                        try {
                            // 第二个参数 true 强制使用 GIS 行业标准的轴顺序：
                            // - 地理坐标系（如 EPSG:4214, EPSG:4326）：经度/纬度 (lon/lat)
                            // - 投影坐标系（如 EPSG:3857）：X/Y (easting/northing)
                            // 这与 ArcGIS 缓存中坐标的存储顺序一致
                            CoordinateReferenceSystem sourceCRS = CRS.decode(config.crs, true);
                            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326", true);

                            GeneralBounds envelope = new GeneralBounds(new double[]{config.fullExtent[0], config.fullExtent[1]},
                                    new double[]{config.fullExtent[2], config.fullExtent[3]});
                            envelope.setCoordinateReferenceSystem(sourceCRS);

                            // 转换到 WGS84
                            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                            GeneralBounds wgs84Envelope = CRS.transform(transform, envelope);

                            minx = wgs84Envelope.getMinimum(0);
                            miny = wgs84Envelope.getMinimum(1);
                            maxx = wgs84Envelope.getMaximum(0);
                            maxy = wgs84Envelope.getMaximum(1);

                            // Also append native BoundingBox
                            xml.append("      <ows:BoundingBox crs=\"").append(config.crs).append("\">\n");
                            xml.append("        <ows:LowerCorner>").append(config.fullExtent[0]).append(" ").append(config.fullExtent[1]).append("</ows:LowerCorner>\n");
                            xml.append("        <ows:UpperCorner>").append(config.fullExtent[2]).append(" ").append(config.fullExtent[3]).append("</ows:UpperCorner>\n");
                            xml.append("      </ows:BoundingBox>\n");

                        } catch (Exception e) {
                            log.warn("Failed to transform extent for layer: " + layer.getName(), e);
                            // Fallback to config extent if it looks like lat/lon? or just keep default
                        }
                    }
                }
            }
        }

        xml.append("      <ows:WGS84BoundingBox>\n");
        xml.append("        <ows:LowerCorner>").append(minx).append(" ").append(miny).append("</ows:LowerCorner>\n");
        xml.append("        <ows:UpperCorner>").append(maxx).append(" ").append(maxy).append("</ows:UpperCorner>\n");
        xml.append("      </ows:WGS84BoundingBox>\n");

        // Style
        xml.append("      <Style isDefault=\"true\">\n");
        xml.append("        <ows:Title>Default Style</ows:Title>\n");
        xml.append("        <ows:Identifier>default</ows:Identifier>\n");
        xml.append("      </Style>\n");

        // Format
        xml.append("      <Format>image/png</Format>\n");
//        xml.append("      <Format>image/jpeg</Format>\n");

        // TileMatrixSetLink
        if (layer.getType() == com.xenon.core.enums.LayerType.ARCGIS_CACHE) {
            // For ArcGIS Cache, use layer-specific TMS
            xml.append("      <TileMatrixSetLink>\n");
            xml.append("        <TileMatrixSet>").append(escapeXml(layer.getName())).append("_TMS</TileMatrixSet>\n");
            xml.append("      </TileMatrixSetLink>\n");
        } else if (layer.getType() == com.xenon.core.enums.LayerType.GEOPACKAGE_TILES) {
            // For GeoPackage Tiles, use layer-specific TMS
            xml.append("      <TileMatrixSetLink>\n");
            xml.append("        <TileMatrixSet>").append(escapeXml(layer.getName())).append("_TMS</TileMatrixSet>\n");
            xml.append("      </TileMatrixSetLink>\n");
        } else {
            // Default TMS for other layers
            xml.append("      <TileMatrixSetLink>\n");
            xml.append("        <TileMatrixSet>EPSG:3857</TileMatrixSet>\n");
            xml.append("      </TileMatrixSetLink>\n");
            xml.append("      <TileMatrixSetLink>\n");
            xml.append("        <TileMatrixSet>EPSG:4326</TileMatrixSet>\n");
            xml.append("      </TileMatrixSetLink>\n");
        }

        // ResourceURL for RESTful access
        String resourceUrl = baseUrl + "/services/" + layer.getName() + "/wmts/{TileMatrixSet}/{TileMatrix}/{TileCol}/{TileRow}";
        xml.append("      <ResourceURL format=\"image/png\" resourceType=\"tile\" template=\"")
                .append(resourceUrl).append(".png\"/>\n");
        //xml.append("      <ResourceURL format=\"image/jpeg\" resourceType=\"tile\" template=\"")
        //        .append(resourceUrl).append(".jpg\"/>\n");

        xml.append("    </Layer>\n");
    }

    private void appendTileMatrixSet(StringBuilder xml, String id, String crs,
                                     double topLeftX, double topLeftY) {
        xml.append("    <TileMatrixSet>\n");
        xml.append("      <ows:Identifier>").append(id).append("</ows:Identifier>\n");
        xml.append("      <ows:SupportedCRS>").append(crs).append("</ows:SupportedCRS>\n");

        // Add tile matrices for zoom levels 0-18
        double scaleDenom = id.contains("3857") ? 559082264.0287178 : 279541132.01435894;
        int tileSize = 256;

        for (int z = 0; z <= 18; z++) {
            int matrixSize = (int) Math.pow(2, z);
            xml.append("      <TileMatrix>\n");
            xml.append("        <ows:Identifier>").append(z).append("</ows:Identifier>\n");
            xml.append("        <ScaleDenominator>").append(scaleDenom / Math.pow(2, z)).append("</ScaleDenominator>\n");
            xml.append("        <TopLeftCorner>").append(topLeftX).append(" ").append(topLeftY).append("</TopLeftCorner>\n");
            xml.append("        <TileWidth>").append(tileSize).append("</TileWidth>\n");
            xml.append("        <TileHeight>").append(tileSize).append("</TileHeight>\n");
            xml.append("        <MatrixWidth>").append(matrixSize).append("</MatrixWidth>\n");
            xml.append("        <MatrixHeight>").append(matrixSize).append("</MatrixHeight>\n");
            xml.append("      </TileMatrix>\n");
        }

        xml.append("    </TileMatrixSet>\n");
    }

    private void appendArcGISRefMatrixSet(StringBuilder xml, Layer layer) {
        // Find cache config
        ArcGISCacheService.CacheConfig config = null;
        if (layer.getDatastoreId() != null) {
            com.xenon.core.entity.DataStore ds = dataStoreService.findById(layer.getDatastoreId());
            if (ds != null && ds.getConnectionParams() != null) {
                String path = (String) ds.getConnectionParams().get("path");
                if (path != null) {
                    config = arcGISCacheService.getCacheConfig(path);
                }
            }
        }

        if (config == null || config.lods.isEmpty()) {
            return;
        }

        xml.append("    <TileMatrixSet>\n");
        xml.append("      <ows:Identifier>").append(escapeXml(layer.getName())).append("_TMS</ows:Identifier>\n");

        // Use parsed CRS or default to 3857
        String crs = config.crs != null ? config.crs : "EPSG:3857";
        // Convert strict EPSG:XXXX to URN format if needed, but simple EPSG:XXXX is also often accepted.
        // OGC recommends URN.
        String crsUrn = crs.startsWith("EPSG:") ? "urn:ogc:def:crs:EPSG::" + crs.substring(5) : crs;
        xml.append("      <ows:SupportedCRS>").append(crsUrn).append("</ows:SupportedCRS>\n");
        double resolutionParam = 1;
        if ("EPSG:4326".equals(crs) || "EPSG:4214".equals(crs) || "EPSG:4610".equals(crs) || "EPSG:4490".equals(crs)) {
            resolutionParam = 111319.49079327358;
        }
        for (ArcGISCacheService.LODInfo lod : config.lods) {
            xml.append("      <TileMatrix>\n");
            xml.append("        <ows:Identifier>").append(lod.level).append("</ows:Identifier>\n");

            // ArcGIS Scale is usually 1:Scale. OGC ScaleDenominator is the same.
            //这里要换成成ogc标准的比例尺
            xml.append("        <ScaleDenominator>").append(1000 * lod.resolution * resolutionParam / 0.28).append("</ScaleDenominator>\n");

            // TopLeftCorner
            if ("EPSG:3857".equals(crs)) {
                xml.append("        <TopLeftCorner>").append(config.x0).append(" ").append(config.y0).append("</TopLeftCorner>\n");
            } else {
                xml.append("        <TopLeftCorner>").append(config.y0).append(" ").append(config.x0).append("</TopLeftCorner>\n");
            }

            xml.append("        <TileWidth>").append(config.tileWidth).append("</TileWidth>\n");
            xml.append("        <TileHeight>").append(config.tileHeight).append("</TileHeight>\n");

            // MatrixWidth/Height
            // If we have fullExtent and resolution, we can calculate.
            long matrixW = 100000;
            long matrixH = 100000;

            if (config.fullExtent != null && lod.resolution > 0) {
                double width = config.fullExtent[2] - config.fullExtent[0];
                double height = config.fullExtent[3] - config.fullExtent[1];
                // This calculation depends on origin position (Top-Left usually matches x0, ymax? or x0, y0?)
                // ArcGIS TileOrigin is usually Top-Left (x0, y0).
                // But wait, if y accounts for "Y goes down", then y0 is top.
                // Assuming standard top-left origin.
                matrixW = (long) Math.ceil(width / (config.tileWidth * lod.resolution));
                matrixH = (long) Math.ceil(height / (config.tileHeight * lod.resolution));
                // Safety
                if (matrixW < 1) {
                    matrixW = 1;
                }
                if (matrixH < 1) {
                    matrixH = 1;
                }
            }

            xml.append("        <MatrixWidth>").append(matrixW).append("</MatrixWidth>\n");
            xml.append("        <MatrixHeight>").append(matrixH).append("</MatrixHeight>\n");

            xml.append("      </TileMatrix>\n");
        }

        xml.append("    </TileMatrixSet>\n");
    }

    /**
     * 添加 GeoPackage 的 TileMatrixSet
     */
    private void appendGeoPackageMatrixSet(StringBuilder xml, Layer layer) {
        if (layer.getDatastoreId() == null) {
            return;
        }

        com.xenon.core.entity.DataStore ds = dataStoreService.findById(layer.getDatastoreId());
        if (ds == null || ds.getConnectionParams() == null) {
            return;
        }

        String gpkgPath = (String) ds.getConnectionParams().get("database");
        if (gpkgPath == null) {
            return;
        }

        // 获取瓦片表名
        String tableName = (String) ds.getConnectionParams().get("table");
        if (tableName == null || tableName.isEmpty()) {
            tableName = layer.getName();
        }

        GeoPackageTileService.GpkgTileConfig config = geoPackageTileService.getConfig(gpkgPath, tableName);
        if (config == null || config.matrices.isEmpty()) {
            return;
        }

        xml.append("    <TileMatrixSet>\n");
        xml.append("      <ows:Identifier>").append(escapeXml(layer.getName())).append("_TMS</ows:Identifier>\n");

        // 使用解析的 CRS
        String crs = config.srs != null ? config.srs : "EPSG:4326";
        String crsUrn = crs.startsWith("EPSG:") ? "urn:ogc:def:crs:EPSG::" + crs.substring(5) : crs;
        xml.append("      <ows:SupportedCRS>").append(crsUrn).append("</ows:SupportedCRS>\n");

        // 计算分辨率参数（用于 ScaleDenominator 计算）
        double resolutionParam = 1;
        if ("EPSG:4326".equals(crs) || crs.startsWith("EPSG:4")) {
            // 地理坐标系，需要转换为米
            resolutionParam = 111319.49079327358;
        }

        // 计算 TopLeftCorner
        double topLeftX = config.bounds != null ? config.bounds[0] : -180;
        double topLeftY = config.bounds != null ? config.bounds[3] : 90;

        for (GeoPackageTileService.TileMatrixInfo matrix : config.matrices) {
            xml.append("      <TileMatrix>\n");
            xml.append("        <ows:Identifier>").append(matrix.zoomLevel).append("</ows:Identifier>\n");

            // 计算 OGC 标准的比例尺
            double scaleDenom = 1000 * matrix.pixelXSize * resolutionParam / 0.28;
            xml.append("        <ScaleDenominator>").append(scaleDenom).append("</ScaleDenominator>\n");

            // TopLeftCorner
            if (crs.contains("3857")) {
                xml.append("        <TopLeftCorner>").append(topLeftX).append(" ").append(topLeftY).append("</TopLeftCorner>\n");
            } else {
                // 地理坐标系，常见的是 lat/lon 顺序
                xml.append("        <TopLeftCorner>").append(topLeftY).append(" ").append(topLeftX).append("</TopLeftCorner>\n");
            }

            xml.append("        <TileWidth>").append(matrix.tileWidth).append("</TileWidth>\n");
            xml.append("        <TileHeight>").append(matrix.tileHeight).append("</TileHeight>\n");
            xml.append("        <MatrixWidth>").append(matrix.matrixWidth).append("</MatrixWidth>\n");
            xml.append("        <MatrixHeight>").append(matrix.matrixHeight).append("</MatrixHeight>\n");
            xml.append("      </TileMatrix>\n");
        }

        xml.append("    </TileMatrixSet>\n");
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
