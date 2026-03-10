package com.xenon.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs for FeatureType REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureTypeDto {
    
    private Long id;
    private String name;
    private String nativeName;
    private String title;
    private String description;
    private String srs;
    private String nativeSrs;
    private Boolean enabled;
    private Double[] nativeBoundingBox;  // [minx, miny, maxx, maxy]
    private Double[] latLonBoundingBox;  // [minx, miny, maxx, maxy]
    private Long datastoreId;

    /**
     * Summary for list responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String name;
        private String href;
    }

    /**
     * List of feature type summaries.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureTypeList {
        private List<Summary> featureTypes;
    }

    /**
     * Wrapper for single feature type responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureTypeWrapper {
        private FeatureTypeDto featureType;
    }
}
