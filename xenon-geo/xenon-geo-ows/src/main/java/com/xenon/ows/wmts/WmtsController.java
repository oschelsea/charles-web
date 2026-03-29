package com.xenon.ows.wmts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * WMTS 服务控制器。
 * 支持 KVP 和 RESTful 两种绑定方式。
 * URL 格式: /services/{workspace}:{layerName}/wmts
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "WMTS", description = "OGC Web Map Tile Service")
@RequestMapping("/xenon")
public class WmtsController {

    private final WmtsService wmtsService;


    /**
     * 图层级别 WMTS KVP 端点。
     * URL: /services/{workspace}:{layerName}/wmts
     */
    @GetMapping(value = "/services/{qualifiedLayer}/wmts")
    @Operation(summary = "WMTS 图层端点", description = "处理指定图层的 WMTS 请求")
    public ResponseEntity<?> handleLayerWmtsKvp(
            HttpServletRequest httpRequest,
            @Parameter(description = "限定图层名 (workspace:layerName)") @PathVariable String qualifiedLayer,
            @Parameter(description = "请求类型") @RequestParam(value = "REQUEST") String request,
            @Parameter(description = "图层名称") @RequestParam(value = "LAYER", required = false) String layer,
            @Parameter(description = "样式名称") @RequestParam(value = "STYLE", required = false) String style,
            @Parameter(description = "瓦片矩阵集") @RequestParam(value = "TILEMATRIXSET", required = false) String tileMatrixSet,
            @Parameter(description = "瓦片矩阵") @RequestParam(value = "TILEMATRIX", required = false) Integer tileMatrix,
            @Parameter(description = "瓦片行") @RequestParam(value = "TILEROW", required = false) Integer tileRow,
            @Parameter(description = "瓦片列") @RequestParam(value = "TILECOL", required = false) Integer tileCol,
            @Parameter(description = "输出格式") @RequestParam(value = "FORMAT", required = false) String format
    ) throws IOException {
        // 如果请求中没有指定 LAYER，使用 URL 中的 qualifiedLayer
        String effectiveLayer = (layer != null && !layer.isEmpty()) ? layer : qualifiedLayer;
        return handleWmtsRequest(httpRequest, qualifiedLayer, request, effectiveLayer, style, tileMatrixSet, tileMatrix, tileRow, tileCol, format);
    }

    /**
     * 处理 WMTS 请求的核心逻辑。
     */
    private ResponseEntity<?> handleWmtsRequest(
            HttpServletRequest httpRequest,
            String qualifiedLayer,
            String request,
            String layer,
            String style,
            String tileMatrixSet,
            Integer tileMatrix,
            Integer tileRow,
            Integer tileCol,
            String format
    ) throws IOException {
        log.debug("WMTS KVP 请求: request={}, layer={}", request, layer);

        return switch (request.toUpperCase()) {
            case "GETCAPABILITIES" -> {
                String baseUrl = wmtsService.getBaseUrl(httpRequest);
                String result = wmtsService.getCapabilities(baseUrl, qualifiedLayer);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(result);
            }
            case "GETTILE" -> {
                byte[] tile = wmtsService.getTile(layer, style, tileMatrixSet,
                        tileMatrix != null ? tileMatrix : 0,
                        tileRow != null ? tileRow : 0,
                        tileCol != null ? tileCol : 0,
                        format);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(
                        format != null ? format : "image/png"));
                headers.setContentLength(tile.length);
                headers.setCacheControl("max-age=31536000");

                yield new ResponseEntity<>(tile, headers, HttpStatus.OK);
            }
            default -> ResponseEntity.badRequest()
                    .body(createException("InvalidRequest", "未知请求类型: " + request));
        };
    }

    /**
     * RESTful 瓦片端点。
     * URL: /services/{workspace}:{layerName}/wmts/{z}/{x}/{y}.{format}
     */
    @GetMapping("/services/{qualifiedLayer}/wmts/{z}/{x}/{y}.{format}")
    @Operation(summary = "WMTS RESTful 瓦片端点", description = "使用简单 XYZ 模式获取瓦片")
    public ResponseEntity<byte[]> getTileSimple(
            @Parameter(description = "限定图层名 (workspace:layerName)") @PathVariable String qualifiedLayer,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            @PathVariable String format
    ) throws IOException {
        log.debug("WMTS REST GetTile: layer={}, z={}, x={}, y={}", qualifiedLayer, z, x, y);

        String mimeType = switch (format.toLowerCase()) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            default -> "image/png";
        };

        byte[] tile = wmtsService.getTile(qualifiedLayer, "default", "EPSG:3857", z, y, x, mimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(tile.length);
        headers.setCacheControl("max-age=31536000");

        return new ResponseEntity<>(tile, headers, HttpStatus.OK);
    }

    /**
     * RESTful 瓦片端点（含瓦片矩阵集）。
     * URL: /services/{workspace}:{layerName}/wmts/{tileMatrixSet}/{z}/{x}/{y}.{format}
     */
    @GetMapping("/services/{qualifiedLayer}/wmts/{tileMatrixSet}/{z}/{x}/{y}.{format}")
    @Operation(summary = "WMTS 瓦片端点（含矩阵集）", description = "使用 TileMatrixSet 模式获取瓦片")
    public ResponseEntity<byte[]> getTileWithMatrixSet(
            @Parameter(description = "限定图层名 (workspace:layerName)") @PathVariable String qualifiedLayer,
            @PathVariable String tileMatrixSet,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            @PathVariable String format
    ) throws IOException {
        log.debug("WMTS REST GetTile: layer={}, tileMatrixSet={}, z={}, x={}, y={}", qualifiedLayer, tileMatrixSet, z, x, y);

        String mimeType = switch (format.toLowerCase()) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            default -> "image/png";
        };

        byte[] tile = wmtsService.getTile(qualifiedLayer, "default", tileMatrixSet, z, y, x, mimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(tile.length);
        headers.setCacheControl("max-age=31536000");

        return new ResponseEntity<>(tile, headers, HttpStatus.OK);
    }

    private String createException(String code, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/1.1\" version=\"1.0.0\">\n" +
                "  <ows:Exception exceptionCode=\"" + code + "\">\n" +
                "    <ows:ExceptionText>" + message + "</ows:ExceptionText>\n" +
                "  </ows:Exception>\n" +
                "</ows:ExceptionReport>";
    }
}

