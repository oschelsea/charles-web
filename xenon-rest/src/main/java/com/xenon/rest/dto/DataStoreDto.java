package com.xenon.rest.dto;

import com.xenon.core.enums.DataStoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for DataStore requests and responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStoreDto {

    private Long id;

    @NotBlank(message = "DataStore name is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$",
             message = "Name must start with a letter and contain only letters, numbers, underscores, or hyphens")
    private String name;

    private String description;

    @NotNull(message = "DataStore type is required")
    private DataStoreType type;

    private Boolean enabled;

    private Map<String, Object> connectionParams;

    private Long workspaceId;

    private String workspaceName;

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
    public static class DataStoreList {
        private List<Summary> dataStores;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataStoreWrapper {
        private DataStoreDto dataStore;
    }
}
