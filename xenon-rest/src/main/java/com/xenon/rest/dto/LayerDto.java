package com.xenon.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Layer requests and responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayerDto {

    private Long id;
    private Long datastoreId;
    private String datastoreName;
    private Long workspaceId;
    private String workspaceName;
    private String name;
    private String title;
    private String description;
    private String type;
    private Boolean enabled;
    private Boolean advertised;
    private Boolean queryable;
    private Boolean opaque;
    private String srs;
    private String defaultStyle;
    private String createdAt;
    private String updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String name;
        private String href;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayerList {
        private List<Summary> layers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayerWrapper {
        private LayerDto layer;
    }
}
