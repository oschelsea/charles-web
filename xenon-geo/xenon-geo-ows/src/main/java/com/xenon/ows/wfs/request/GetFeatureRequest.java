package com.xenon.ows.wfs.request;

import lombok.Builder;
import lombok.Data;

/**
 * WFS GetFeature request parameters.
 */
@Data
@Builder
public class GetFeatureRequest {
    private String service;
    private String request;
    private String version;
    private String typeName;
    private String typeNames;  // WFS 2.0
    private String outputFormat;
    private Integer maxFeatures;  // WFS 1.x
    private Integer count;         // WFS 2.0
    private Integer startIndex;
    private String srsName;
    private String bbox;
    private String filter;
    private String cqlFilter;
    private String propertyName;
    private String sortBy;
    
    public String getEffectiveTypeName() {
        return typeName != null ? typeName : typeNames;
    }
    
    public int getEffectiveMaxFeatures() {
        if (count != null) return count;
        if (maxFeatures != null) return maxFeatures;
        return 1000; // Default
    }
}
