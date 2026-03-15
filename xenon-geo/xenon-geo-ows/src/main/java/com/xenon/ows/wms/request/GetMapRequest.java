package com.xenon.ows.wms.request;

import lombok.Builder;
import lombok.Data;

import java.awt.*;

/**
 * WMS GetMap request parameters.
 */
@Data
@Builder
public class GetMapRequest {
    
    /**
     * Service type (always "WMS")
     */
    private String service;
    
    /**
     * Request type (always "GetMap")
     */
    private String request;
    
    /**
     * WMS version (1.1.1 or 1.3.0)
     */
    private String version;
    
    /**
     * Comma-separated list of layer names
     */
    private String layers;
    
    /**
     * Comma-separated list of style names
     */
    private String styles;
    
    /**
     * Coordinate Reference System (CRS for 1.3.0, SRS for 1.1.1)
     */
    private String crs;
    
    /**
     * Bounding box (minx,miny,maxx,maxy)
     */
    private String bbox;
    
    /**
     * Output image width in pixels
     */
    private int width;
    
    /**
     * Output image height in pixels
     */
    private int height;
    
    /**
     * Output format MIME type (e.g., image/png)
     */
    private String format;
    
    /**
     * Whether background should be transparent
     */
    private boolean transparent;
    
    /**
     * Background color (hex format)
     */
    private Color bgColor;
    
    /**
     * Exception format
     */
    private String exceptions;
    
    /**
     * Time parameter for temporal data
     */
    private String time;
    
    /**
     * Elevation parameter
     */
    private String elevation;
    
    /**
     * CQL filter expression
     */
    private String cqlFilter;
    
    /**
     * Parse bounding box string to array of doubles.
     */
    public double[] getBboxArray() {
        if (bbox == null || bbox.isEmpty()) {
            return null;
        }
        String[] parts = bbox.split(",");
        if (parts.length != 4) {
            return null;
        }
        return new double[] {
            Double.parseDouble(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3])
        };
    }
}
