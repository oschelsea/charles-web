package com.xenon.ows.wms.request;

import lombok.Builder;
import lombok.Data;

/**
 * WMS GetCapabilities request parameters.
 */
@Data
@Builder
public class GetCapabilitiesRequest {
    
    /**
     * Service type (always "WMS")
     */
    private String service;
    
    /**
     * Request type (always "GetCapabilities")
     */
    private String request;
    
    /**
     * WMS version (1.1.1 or 1.3.0)
     */
    private String version;
    
    /**
     * Output format for capabilities document
     */
    private String format;
    
    /**
     * Update sequence for cache validation
     */
    private String updateSequence;
}
