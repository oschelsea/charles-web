package com.xenon.rest.dto;

import com.xenon.core.enums.StyleFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Style requests and responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StyleDto {

    private Long id;

    @NotBlank(message = "Style name is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", 
             message = "Name must start with a letter and contain only letters, numbers, underscores, or hyphens")
    private String name;

    private String title;

    private String description;

    @NotNull
    private StyleFormat format;

    private String content;

    private String filename;

    private Long workspaceId;

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
    public static class StyleList {
        private List<Summary> styles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleWrapper {
        private StyleDto style;
    }
}
