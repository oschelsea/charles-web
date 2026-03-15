package com.xenon.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Workspace requests and responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDto {

    private Long id;

    @NotBlank(message = "Workspace name is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", 
             message = "Name must start with a letter and contain only letters, numbers, underscores, or hyphens")
    private String name;

    private String namespaceUri;

    private String description;

    private Boolean isolated;

    private Boolean enabled;

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
    public static class WorkspaceList {
        private List<Summary> workspaces;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkspaceWrapper {
        private WorkspaceDto workspace;
    }
}
