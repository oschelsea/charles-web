package com.xenon.ows.wfs.request;

import lombok.Builder;
import lombok.Data;

/**
 * WFS DescribeFeatureType request parameters.
 */
@Data
@Builder
public class DescribeFeatureTypeRequest {
    private String service;
    private String request;
    private String version;
    private String typeName;
    private String outputFormat;
}
