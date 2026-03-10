package com.xenon.ows.wms.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * WMS Capabilities response data.
 */
@Data
@Builder
public class WmsCapabilities {
    
    private String version;
    private String serviceTitle;
    private String serviceAbstract;
    private String onlineResource;
    private String contactPerson;
    private String contactOrganization;
    private String contactEmail;
    
    @Builder.Default
    private List<String> formats = new ArrayList<>(List.of(
            "image/png",
            "image/jpeg",
            "image/gif"
    ));
    
    @Builder.Default
    private List<String> infoFormats = new ArrayList<>(List.of(
            "text/plain",
            "text/html",
            "application/json",
            "application/vnd.ogc.gml"
    ));
    
    @Builder.Default
    private List<WmsLayer> layers = new ArrayList<>();
    
    /**
     * Represents a layer in the capabilities document.
     */
    @Data
    @Builder
    public static class WmsLayer {
        private String name;
        private String title;
        private String abstractText;
        private List<String> crs;
        private double[] bbox; // minx, miny, maxx, maxy
        private boolean queryable;
        private boolean opaque;
        private List<WmsStyle> styles;
    }
    
    /**
     * Represents a style in the capabilities document.
     */
    @Data
    @Builder
    public static class WmsStyle {
        private String name;
        private String title;
        private String abstractText;
        private String legendUrl;
    }
}
