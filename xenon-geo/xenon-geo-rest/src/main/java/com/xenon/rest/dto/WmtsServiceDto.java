package com.xenon.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for WMTS service information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmtsServiceDto {

    private String title;
    private String description;
    private String version;
    private String capabilitiesUrl;
    private List<WmtsLayerInfo> layers;
    private List<TileMatrixSetInfo> tileMatrixSets;

    /**
     * WMTS layer information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WmtsLayerInfo {
        private String name;
        private String title;
        private String description;
        private String type;
        private boolean enabled;
        private String tileUrl;
        private List<String> formats;
        private List<String> tileMatrixSets;
    }

    /**
     * Tile matrix set information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TileMatrixSetInfo {
        private String identifier;
        private String supportedCRS;
        private int minZoom;
        private int maxZoom;
        private int tileWidth;
        private int tileHeight;
    }

    /**
     * Wrapper for layer list response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayerListWrapper {
        private List<WmtsLayerInfo> layers;
    }

    /**
     * Wrapper for single layer response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayerWrapper {
        private WmtsLayerInfo layer;
    }
}
