package com.xenon.ows.wmts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ArcGISCacheService {

    private final Map<String, CacheConfig> cacheConfigs = new ConcurrentHashMap<>();

    public static class CacheConfig {
        /**
         * 缓存格式: PNG, JPEG, MIXED
         */
        public String format;
        public int tileWidth = 256;
        public int tileHeight = 256;
        public double x0;
        public double y0;
        /**
         * 每个 Bundle 的瓦片行列数，默认为 128
         */
        public int packetSize = 128;
        public boolean isCompact = false;
        public java.util.List<LODInfo> lods = new java.util.ArrayList<>();
        /**
         * 坐标系，例如 "EPSG:3857"
         */
        public String crs;
        /**
         * 全图范围 [minx, miny, maxx, maxy]
         */
        public double[] fullExtent;
    }

    public static class LODInfo {
        public int level;
        public double resolution;
        public double scale;
    }

    /**
     * 解析 conf.xml 以获取缓存配置
     */
    public CacheConfig getCacheConfig(String cachePath) {
        if (cacheConfigs.containsKey(cachePath)) {
            return cacheConfigs.get(cachePath);
        }

        CacheConfig config = new CacheConfig();
        File confFile = new File(cachePath);

        if (confFile.exists()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(confFile);

                NodeList formatNodes = doc.getElementsByTagName("TileImageInfo");
                if (formatNodes.getLength() > 0) {
                    Element formatElem = (Element) formatNodes.item(0);
                    config.format = getTagValue("CacheTileFormat", formatElem);
                }

                // Fallback format
                if (config.format == null) {
                    config.format = "PNG";
                }

                NodeList originNodes = doc.getElementsByTagName("TileOrigin");
                if (originNodes.getLength() > 0) {
                    Element originElem = (Element) originNodes.item(0);
                    String x = getTagValue("X", originElem);
                    String y = getTagValue("Y", originElem);
                    if (x != null) {
                        config.x0 = Double.parseDouble(x);
                    }
                    if (y != null) {
                        config.y0 = Double.parseDouble(y);
                    }
                }

                // Parse Spatial Reference
                NodeList spatialRefNodes = doc.getElementsByTagName("SpatialReference");
                if (spatialRefNodes.getLength() > 0) {
                    Element srElem = (Element) spatialRefNodes.item(0);
                    String wkid = getTagValue("WKID", srElem);
                    String wkt = getTagValue("WKT", srElem);
                    if (wkid != null && !wkid.isEmpty()) {
                        config.crs = "EPSG:" + wkid;
                    } else if (wkt != null && !wkt.isEmpty()) {
                        // Rough check for common WKTs if needed, or just store WKT?
                        // WMTS usually wants EPSG URI.
                        // For now, if no WKID, we might leave it null or try to guess.
                    }
                }

                // 解析范围信息: FullExtent 或 InitialExtent
                // 通常在 CacheInfo -> TileCacheInfo -> FullExtent 中
                NodeList extentNodes = doc.getElementsByTagName("FullExtent");
                if (extentNodes.getLength() == 0) {
                    extentNodes = doc.getElementsByTagName("InitialExtent");
                }

                if (extentNodes.getLength() > 0) {
                    Element extElem = (Element) extentNodes.item(0);
                    String xmin = getTagValue("XMin", extElem);
                    String ymin = getTagValue("YMin", extElem);
                    String xmax = getTagValue("XMax", extElem);
                    String ymax = getTagValue("YMax", extElem);

                    if (xmin != null && ymin != null && xmax != null && ymax != null) {
                        config.fullExtent = new double[]{
                                Double.parseDouble(xmin),
                                Double.parseDouble(ymin),
                                Double.parseDouble(xmax),
                                Double.parseDouble(ymax)
                        };
                    }
                }

                // 如果 Conf.xml 中没有范围信息，尝试从同级目录的 conf.cdi 文件读取
                if (config.fullExtent == null) {
                    config.fullExtent = parseExtentFromCdiFile(confFile.getParentFile());
                }

                // Parse LODs (TileCacheInfo -> LODInfos -> LODInfo)
                NodeList lodInfoNodes = doc.getElementsByTagName("LODInfo");
                for (int i = 0; i < lodInfoNodes.getLength(); i++) {
                    Element lodElem = (Element) lodInfoNodes.item(i);
                    LODInfo lod = new LODInfo();

                    String levelID = getTagValue("LevelID", lodElem);
                    String scale = getTagValue("Scale", lodElem);
                    String resolution = getTagValue("Resolution", lodElem);

                    if (levelID != null) {
                        lod.level = Integer.parseInt(levelID);
                    }
                    if (scale != null) {
                        lod.scale = Double.parseDouble(scale);
                    }
                    if (resolution != null) {
                        lod.resolution = Double.parseDouble(resolution);
                    }

                    config.lods.add(lod);
                }
                // Sort by level just in case
                config.lods.sort((a, b) -> Integer.compare(a.level, b.level));

                // Check storage format (Exploded vs Compact)
                NodeList storageNodes = doc.getElementsByTagName("CacheStorageInfo");
                if (storageNodes.getLength() > 0) {
                    Element storageElem = (Element) storageNodes.item(0);
                    String format = getTagValue("StorageFormat", storageElem);
                    if (format != null && format.startsWith("esriMapCacheStorageModeCompact")) {
                        config.isCompact = true;
                        String packetSize = getTagValue("PacketSize", storageElem);
                        if (packetSize != null) {
                            config.packetSize = Integer.parseInt(packetSize);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("Error parsing ArcGIS conf.xml", e);
            }
        }

        // 自动检测是否为紧凑型缓存
        // 如果配置未明确但存在 _alllayers 文件夹
        File parentDir = confFile.getParentFile();
        File diffLayer = new File(parentDir, "_alllayers");
        if (!diffLayer.exists()) {
            // 尝试 "Layers" 目录（旧版本缓存或简单结构）
            diffLayer = new File(parentDir, "Layers");
        }

        cacheConfigs.put(cachePath, config);
        return config;
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    /**
     * 从 conf.cdi 文件解析范围信息
     * conf.cdi 文件包含 EnvelopeN 元素，其中有 XMin, YMin, XMax, YMax 坐标
     *
     * @param parentDir conf.xml 所在的目录
     * @return 范围数组 [minx, miny, maxx, maxy]，如果解析失败返回 null
     */
    private double[] parseExtentFromCdiFile(File parentDir) {
        File cdiFile = new File(parentDir, "conf.cdi");
        if (!cdiFile.exists()) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cdiFile);

            // conf.cdi 的根元素是 EnvelopeN，直接从根元素获取坐标
            Element root = doc.getDocumentElement();
            String xmin = getTagValue("XMin", root);
            String ymin = getTagValue("YMin", root);
            String xmax = getTagValue("XMax", root);
            String ymax = getTagValue("YMax", root);

            if (xmin != null && ymin != null && xmax != null && ymax != null) {
                return new double[]{
                        Double.parseDouble(xmin),
                        Double.parseDouble(ymin),
                        Double.parseDouble(xmax),
                        Double.parseDouble(ymax)
                };
            }
        } catch (Exception e) {
            log.warn("解析 conf.cdi 文件失败: {}", cdiFile.getAbsolutePath(), e);
        }

        return null;
    }

    public byte[] getTile(String cachePath, int z, int row, int col) throws IOException {
        CacheConfig config = getCacheConfig(cachePath);

        // 确定图层基础目录
        File confFile = new File(cachePath);
        File parentDir = confFile.getParentFile();

        File layersDir = new File(parentDir, "_alllayers");
        if (!layersDir.exists()) {
            // 有时是嵌套结构
            layersDir = new File(parentDir, "Layers/_alllayers");
            if (!layersDir.exists()) {
                // 或者直接是 Layers
                layersDir = new File(parentDir, "Layers");
            }
        }

        if (config.isCompact) {
            return getCompactTile(layersDir, z, row, col, config);
        } else {
            return getLooseTile(layersDir, z, row, col, config);
        }
    }

    private byte[] getLooseTile(File layersDir, int z, int row, int col, CacheConfig config) throws IOException {
        // 格式: L<z>/R<row_hex>/C<col_hex>.<ext>
        // Zoom 通常是 L00, L01...
        // Row/Col 是 8 位十六进制字符串

        String levelStr = String.format("L%02d", z);
        String rowStr = String.format("R%08x", row);
        String colStr = String.format("C%08x", col);

        String ext = "png";
        if (config.format != null) {
            if (config.format.toUpperCase().contains("JPEG") || config.format.toUpperCase().contains("JPG")) {
                ext = "jpg";
            } else if (config.format.toUpperCase().contains("MIXED")) {
                // 混合模式尝试 png
                ext = "png";
            }
        }

        File tileFile = Paths.get(layersDir.getAbsolutePath(), levelStr, rowStr, colStr + "." + ext).toFile();

        // If MIXED, check jpg if png missing
        if (!tileFile.exists() && "MIXED".equalsIgnoreCase(config.format)) {
            tileFile = Paths.get(layersDir.getAbsolutePath(), levelStr, rowStr, colStr + ".jpg").toFile();
        }

        if (tileFile.exists()) {
            return Files.readAllBytes(tileFile.toPath());
        }

        return null;
    }

    private byte[] getCompactTile(File layersDir, int z, int row, int col, CacheConfig config) throws IOException {
        // Compact V2 实现 (单个 .bundle 文件)
        // 分组大小为 packetSize (默认 128)

        int rGroup = (row / config.packetSize) * config.packetSize;
        int cGroup = (col / config.packetSize) * config.packetSize;

        String levelStr = String.format("L%02d", z);
        String bundleName = String.format("R%04xC%04x.bundle", rGroup, cGroup);

        File bundleFile = Paths.get(layersDir.getAbsolutePath(), levelStr, bundleName).toFile();

        if (!bundleFile.exists()) {
            // 尝试小写 'r' 和 'c'
            bundleName = String.format("r%04xc%04x.bundle", rGroup, cGroup);
            bundleFile = Paths.get(layersDir.getAbsolutePath(), levelStr, bundleName).toFile();

            if (!bundleFile.exists()) {
                log.debug("未找到 Bundle 文件: {}", bundleFile.getPath());
                return null;
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(bundleFile, "r")) {
            // V2 Bundle 有 64 字节头部
            // 索引区从 64 字节开始

            // 计算包内索引
            int indexInBundle = (row % config.packetSize) * config.packetSize + (col % config.packetSize);
            long indexOffset = 64 + (long) indexInBundle * 8;

            raf.seek(indexOffset);
            byte[] indexEntry = new byte[8];
            raf.readFully(indexEntry);

            ByteBuffer buffer = ByteBuffer.wrap(indexEntry).order(ByteOrder.LITTLE_ENDIAN);

            // Java Long 是 8 字节
            // 结构: Offset (5 字节), Size (3 字节)
            // 组合: (offset) | (size << 40)

            long combined = buffer.getLong(0);
            long dataOffset = combined & 0xFFFFFFFFFFL;
            int dataSize = (int) (combined >>> 40);

            if (dataSize <= 0) {
                return null;
            }

            byte[] tileData = new byte[dataSize];
            raf.seek(dataOffset);
            raf.readFully(tileData);

            // 通常标准图像数据直接存储，无需额外剥离头部
            return tileData;
        } catch (Exception e) {
            log.error("读取紧凑型瓦片失败", e);
            return null;
        }
    }
}
