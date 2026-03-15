package com.xenon.ows.wms;

import com.xenon.ows.wms.request.GetCapabilitiesRequest;
import com.xenon.ows.wms.request.GetFeatureInfoRequest;
import com.xenon.ows.wms.request.GetMapRequest;
import com.xenon.ows.wms.response.WmsCapabilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.IOException;

/**
 * WMS 服务控制器。
 * 处理 WMS 1.1.1 和 1.3.0 请求。
 * URL 格式: /services/{workspace}:{layerName}/wms
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "WMS", description = "OGC Web Map Service")
public class WmsController {

    private final WmsService wmsService;

    /**
     * WMS 图层端点。
     * URL: /services/{workspace}:{layerName}/wms
     */
    @GetMapping(value = "/services/{qualifiedLayer}/wms")
    @Operation(summary = "WMS 服务端点", description = "处理 WMS GetCapabilities、GetMap 和 GetFeatureInfo 请求")
    public ResponseEntity<?> handleWms(
            @Parameter(description = "限定图层名 (workspace:layerName)") @PathVariable String qualifiedLayer,
            @Parameter(description = "服务类型 (WMS)") @RequestParam(value = "SERVICE", required = false) String service,
            @Parameter(description = "请求类型") @RequestParam(value = "REQUEST") String request,
            @Parameter(description = "WMS 版本") @RequestParam(value = "VERSION", required = false) String version,
            @Parameter(description = "图层名称") @RequestParam(value = "LAYERS", required = false) String layers,
            @Parameter(description = "样式名称") @RequestParam(value = "STYLES", required = false) String styles,
            @Parameter(description = "CRS/SRS") @RequestParam(value = "CRS", required = false) String crs,
            @RequestParam(value = "SRS", required = false) String srs,
            @Parameter(description = "边界框") @RequestParam(value = "BBOX", required = false) String bbox,
            @Parameter(description = "图像宽度") @RequestParam(value = "WIDTH", required = false, defaultValue = "256") int width,
            @Parameter(description = "图像高度") @RequestParam(value = "HEIGHT", required = false, defaultValue = "256") int height,
            @Parameter(description = "输出格式") @RequestParam(value = "FORMAT", required = false, defaultValue = "image/png") String format,
            @Parameter(description = "透明背景") @RequestParam(value = "TRANSPARENT", required = false, defaultValue = "true") boolean transparent,
            @Parameter(description = "背景颜色") @RequestParam(value = "BGCOLOR", required = false) String bgcolor,
            @Parameter(description = "查询图层") @RequestParam(value = "QUERY_LAYERS", required = false) String queryLayers,
            @Parameter(description = "信息格式") @RequestParam(value = "INFO_FORMAT", required = false) String infoFormat,
            @Parameter(description = "X 坐标 (WMS 1.1.1)") @RequestParam(value = "X", required = false, defaultValue = "0") int x,
            @Parameter(description = "Y 坐标 (WMS 1.1.1)") @RequestParam(value = "Y", required = false, defaultValue = "0") int y,
            @Parameter(description = "I 坐标 (WMS 1.3.0)") @RequestParam(value = "I", required = false) Integer i,
            @Parameter(description = "J 坐标 (WMS 1.3.0)") @RequestParam(value = "J", required = false) Integer j,
            @Parameter(description = "要素数量") @RequestParam(value = "FEATURE_COUNT", required = false, defaultValue = "1") int featureCount,
            @Parameter(description = "CQL 过滤器") @RequestParam(value = "CQL_FILTER", required = false) String cqlFilter
    ) throws IOException {
        
        log.debug("WMS 请求: qualifiedLayer={}, request={}", qualifiedLayer, request);
        
        // 如果请求中没有指定 LAYERS，使用 URL 中的 qualifiedLayer
        String effectiveLayers = (layers != null && !layers.isEmpty()) ? layers : qualifiedLayer;
        
        // 根据版本使用 CRS 或 SRS
        String coordinateSystem = crs != null ? crs : srs;
        
        return switch (request.toUpperCase()) {
            case "GETCAPABILITIES" -> handleGetCapabilities(version);
            case "GETMAP" -> handleGetMap(effectiveLayers, styles, coordinateSystem, bbox, width, height, 
                    format, transparent, bgcolor, cqlFilter);
            case "GETFEATUREINFO" -> handleGetFeatureInfo(queryLayers, effectiveLayers, styles, coordinateSystem,
                    bbox, width, height, i != null ? i : x, j != null ? j : y, infoFormat, featureCount);
            default -> ResponseEntity.badRequest()
                    .body(createServiceException("InvalidRequest", "未知请求类型: " + request));
        };
    }

    private ResponseEntity<String> handleGetCapabilities(String version) {
        GetCapabilitiesRequest request = GetCapabilitiesRequest.builder()
                .version(version)
                .build();
        
        WmsCapabilities capabilities = wmsService.getCapabilities(request);
        String xml = generateCapabilitiesXml(capabilities);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    private ResponseEntity<byte[]> handleGetMap(
            String layers, String styles, String crs, String bbox,
            int width, int height, String format, boolean transparent,
            String bgcolor, String cqlFilter) throws IOException {
        
        GetMapRequest request = GetMapRequest.builder()
                .layers(layers)
                .styles(styles)
                .crs(crs)
                .bbox(bbox)
                .width(Math.min(width, 4096))  // Limit max size
                .height(Math.min(height, 4096))
                .format(format)
                .transparent(transparent)
                .bgColor(parseColor(bgcolor))
                .cqlFilter(cqlFilter)
                .build();
        
        byte[] imageData = wmsService.getMap(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(format));
        headers.setContentLength(imageData.length);
        
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }

    private ResponseEntity<String> handleGetFeatureInfo(
            String queryLayers, String layers, String styles, String crs,
            String bbox, int width, int height, int x, int y,
            String infoFormat, int featureCount) {
        
        GetFeatureInfoRequest request = GetFeatureInfoRequest.builder()
                .queryLayers(queryLayers)
                .layers(layers)
                .styles(styles)
                .crs(crs)
                .bbox(bbox)
                .width(width)
                .height(height)
                .x(x)
                .y(y)
                .infoFormat(infoFormat != null ? infoFormat : "text/plain")
                .featureCount(featureCount)
                .build();
        
        String result = wmsService.getFeatureInfo(request);
        
        MediaType mediaType = infoFormat != null && infoFormat.contains("json") 
                ? MediaType.APPLICATION_JSON 
                : MediaType.APPLICATION_XML;
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(result);
    }

    private Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return null;
        }
        try {
            if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
                return new Color(Integer.parseInt(colorStr.substring(2), 16));
            }
            return Color.decode(colorStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String generateCapabilitiesXml(WmsCapabilities capabilities) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<WMS_Capabilities version=\"").append(capabilities.getVersion()).append("\" ");
        xml.append("xmlns=\"http://www.opengis.net/wms\" ");
        xml.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        
        // Service section
        xml.append("  <Service>\n");
        xml.append("    <Name>WMS</Name>\n");
        xml.append("    <Title>").append(escapeXml(capabilities.getServiceTitle())).append("</Title>\n");
        xml.append("    <Abstract>").append(escapeXml(capabilities.getServiceAbstract())).append("</Abstract>\n");
        xml.append("    <OnlineResource xlink:href=\"").append(capabilities.getOnlineResource()).append("\"/>\n");
        xml.append("    <ContactInformation>\n");
        xml.append("      <ContactPersonPrimary>\n");
        xml.append("        <ContactPerson>").append(escapeXml(capabilities.getContactPerson())).append("</ContactPerson>\n");
        xml.append("        <ContactOrganization>").append(escapeXml(capabilities.getContactOrganization())).append("</ContactOrganization>\n");
        xml.append("      </ContactPersonPrimary>\n");
        xml.append("    </ContactInformation>\n");
        xml.append("  </Service>\n");
        
        // Capability section
        xml.append("  <Capability>\n");
        xml.append("    <Request>\n");
        
        // GetCapabilities
        xml.append("      <GetCapabilities>\n");
        xml.append("        <Format>application/vnd.ogc.wms_xml</Format>\n");
        xml.append("        <DCPType><HTTP><Get><OnlineResource xlink:href=\"")
           .append(capabilities.getOnlineResource()).append("?\"/></Get></HTTP></DCPType>\n");
        xml.append("      </GetCapabilities>\n");
        
        // GetMap
        xml.append("      <GetMap>\n");
        for (String format : capabilities.getFormats()) {
            xml.append("        <Format>").append(format).append("</Format>\n");
        }
        xml.append("        <DCPType><HTTP><Get><OnlineResource xlink:href=\"")
           .append(capabilities.getOnlineResource()).append("?\"/></Get></HTTP></DCPType>\n");
        xml.append("      </GetMap>\n");
        
        // GetFeatureInfo
        xml.append("      <GetFeatureInfo>\n");
        for (String format : capabilities.getInfoFormats()) {
            xml.append("        <Format>").append(format).append("</Format>\n");
        }
        xml.append("        <DCPType><HTTP><Get><OnlineResource xlink:href=\"")
           .append(capabilities.getOnlineResource()).append("?\"/></Get></HTTP></DCPType>\n");
        xml.append("      </GetFeatureInfo>\n");
        
        xml.append("    </Request>\n");
        xml.append("    <Exception><Format>XML</Format></Exception>\n");
        
        // Layer section
        xml.append("    <Layer>\n");
        xml.append("      <Title>Xenon Layers</Title>\n");
        xml.append("      <CRS>EPSG:4326</CRS>\n");
        xml.append("      <CRS>EPSG:3857</CRS>\n");
        xml.append("      <EX_GeographicBoundingBox>\n");
        xml.append("        <westBoundLongitude>-180</westBoundLongitude>\n");
        xml.append("        <eastBoundLongitude>180</eastBoundLongitude>\n");
        xml.append("        <southBoundLatitude>-90</southBoundLatitude>\n");
        xml.append("        <northBoundLatitude>90</northBoundLatitude>\n");
        xml.append("      </EX_GeographicBoundingBox>\n");
        
        // Add layers
        for (WmsCapabilities.WmsLayer layer : capabilities.getLayers()) {
            appendLayerXml(xml, layer);
        }
        
        xml.append("    </Layer>\n");
        xml.append("  </Capability>\n");
        xml.append("</WMS_Capabilities>");
        
        return xml.toString();
    }

    private void appendLayerXml(StringBuilder xml, WmsCapabilities.WmsLayer layer) {
        xml.append("      <Layer queryable=\"").append(layer.isQueryable() ? "1" : "0").append("\">\n");
        xml.append("        <Name>").append(escapeXml(layer.getName())).append("</Name>\n");
        xml.append("        <Title>").append(escapeXml(layer.getTitle())).append("</Title>\n");
        if (layer.getAbstractText() != null) {
            xml.append("        <Abstract>").append(escapeXml(layer.getAbstractText())).append("</Abstract>\n");
        }
        if (layer.getBbox() != null && layer.getBbox().length == 4) {
            xml.append("        <BoundingBox CRS=\"EPSG:4326\" ");
            xml.append("minx=\"").append(layer.getBbox()[0]).append("\" ");
            xml.append("miny=\"").append(layer.getBbox()[1]).append("\" ");
            xml.append("maxx=\"").append(layer.getBbox()[2]).append("\" ");
            xml.append("maxy=\"").append(layer.getBbox()[3]).append("\"/>\n");
        }
        xml.append("      </Layer>\n");
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    private String createServiceException(String code, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ServiceExceptionReport version=\"1.3.0\">\n" +
                "  <ServiceException code=\"" + code + "\">" + escapeXml(message) + "</ServiceException>\n" +
                "</ServiceExceptionReport>";
    }
}
