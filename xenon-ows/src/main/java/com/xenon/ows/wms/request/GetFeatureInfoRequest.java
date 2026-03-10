package com.xenon.ows.wms.request;

import lombok.Builder;
import lombok.Data;

/**
 * WMS GetFeatureInfo request parameters.
 */
@Data
@Builder
public class GetFeatureInfoRequest {
    
    /**
     * Service type (always "WMS")
     */
    private String service;
    
    /**
     * Request type (always "GetFeatureInfo")
     */
    private String request;
    
    /**
     * WMS version
     */
    private String version;
    
    /**
     * Comma-separated list of layer names to query
     */
    private String queryLayers;
    
    /**
     * All layers in the GetMap request
     */
    private String layers;
    
    /**
     * Styles for the layers
     */
    private String styles;
    
    /**
     * Coordinate Reference System
     */
    private String crs;
    
    /**
     * Bounding box
     */
    private String bbox;
    
    /**
     * Image width
     */
    private int width;
    
    /**
     * Image height
     */
    private int height;
    
    /**
     * X coordinate of query point (in pixels)
     * Named "I" in WMS 1.3.0, "X" in WMS 1.1.1
     */
    private int x;
    
    /**
     * Y coordinate of query point (in pixels)
     * Named "J" in WMS 1.3.0, "Y" in WMS 1.1.1
     */
    private int y;
    
    /**
     * Output format for feature info (e.g., text/html, application/json)
     */
    private String infoFormat;
    
    /**
     * Maximum number of features to return
     */
    private int featureCount;
    
    /**
     * Exception format
     */
    private String exceptions;
}
