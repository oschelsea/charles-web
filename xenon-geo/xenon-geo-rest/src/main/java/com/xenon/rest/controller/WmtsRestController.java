package com.xenon.rest.controller;

import com.xenon.core.entity.Layer;
import com.xenon.ows.wmts.WmtsService;
import com.xenon.rest.dto.WmtsServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * REST API controller for WMTS service management.
 */
@RestController
@RequestMapping("/api/v1/wmts")
@RequiredArgsConstructor
@Tag(name = "WMTS Service", description = "WMTS service management operations")
public class WmtsRestController {

    private final WmtsService wmtsService;

    @GetMapping("/capabilities")
    @Operation(summary = "Get WMTS service information", description = "Returns WMTS service metadata and configuration")
    public ResponseEntity<WmtsServiceDto> getServiceInfo(HttpServletRequest request) {
        String baseUrl = wmtsService.getBaseUrl(request);
        List<Layer> layers = wmtsService.getWmtsLayers();
        
        WmtsServiceDto dto = WmtsServiceDto.builder()
                .title("Xenon WMTS")
                .description("Web Map Tile Service provided by Xenon")
                .version("1.0.0")
                .capabilitiesUrl(baseUrl + "/wmts?SERVICE=WMTS&REQUEST=GetCapabilities")
                .layers(layers.stream().map(layer -> toLayerInfo(layer, baseUrl)).toList())
                .tileMatrixSets(getDefaultTileMatrixSets())
                .build();
        
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/layers")
    @Operation(summary = "Get WMTS layers", description = "Returns list of layers available via WMTS")
    public ResponseEntity<WmtsServiceDto.LayerListWrapper> getLayers(HttpServletRequest request) {
        String baseUrl = wmtsService.getBaseUrl(request);
        List<Layer> layers = wmtsService.getWmtsLayers();
        
        List<WmtsServiceDto.WmtsLayerInfo> layerInfos = layers.stream()
                .map(layer -> toLayerInfo(layer, baseUrl))
                .toList();
        
        return ResponseEntity.ok(WmtsServiceDto.LayerListWrapper.builder()
                .layers(layerInfos)
                .build());
    }

    @GetMapping("/layers/{name}")
    @Operation(summary = "Get WMTS layer by name", description = "Returns WMTS information for a specific layer")
    public ResponseEntity<WmtsServiceDto.LayerWrapper> getLayerByName(
            @Parameter(description = "Layer name") @PathVariable String name,
            HttpServletRequest request) {
        String baseUrl = wmtsService.getBaseUrl(request);
        List<Layer> layers = wmtsService.getWmtsLayers();
        
        Layer layer = layers.stream()
                .filter(l -> l.getName().equals(name))
                .findFirst()
                .orElse(null);
        
        if (layer == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(WmtsServiceDto.LayerWrapper.builder()
                .layer(toLayerInfo(layer, baseUrl))
                .build());
    }

    private WmtsServiceDto.WmtsLayerInfo toLayerInfo(Layer layer, String baseUrl) {
        String tileUrl = baseUrl + "/wmts/" + layer.getName() + "/{TileMatrixSet}/{z}/{x}/{y}.png";
        
        return WmtsServiceDto.WmtsLayerInfo.builder()
                .name(layer.getName())
                .title(layer.getTitle() != null ? layer.getTitle() : layer.getName())
                .description(layer.getDescription())
                .type(layer.getType() != null ? layer.getType().name() : null)
                .enabled(layer.getEnabled() != null ? layer.getEnabled() : true)
                .tileUrl(tileUrl)
                .formats(Arrays.asList("image/png", "image/jpeg"))
                .tileMatrixSets(Arrays.asList("EPSG:3857", "EPSG:4326"))
                .build();
    }

    private List<WmtsServiceDto.TileMatrixSetInfo> getDefaultTileMatrixSets() {
        return Arrays.asList(
                WmtsServiceDto.TileMatrixSetInfo.builder()
                        .identifier("EPSG:3857")
                        .supportedCRS("urn:ogc:def:crs:EPSG::3857")
                        .minZoom(0)
                        .maxZoom(18)
                        .tileWidth(256)
                        .tileHeight(256)
                        .build(),
                WmtsServiceDto.TileMatrixSetInfo.builder()
                        .identifier("EPSG:4326")
                        .supportedCRS("urn:ogc:def:crs:EPSG::4326")
                        .minZoom(0)
                        .maxZoom(18)
                        .tileWidth(256)
                        .tileHeight(256)
                        .build()
        );
    }
}
