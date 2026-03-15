package com.xenon.rest.controller;

import com.xenon.core.entity.DataStore;
import com.xenon.core.entity.FeatureType;
import com.xenon.core.entity.Layer;
import com.xenon.core.entity.Workspace;
import com.xenon.core.enums.DataStoreType;
import com.xenon.core.enums.LayerType;
import com.xenon.core.service.DataStoreService;
import com.xenon.core.service.FeatureTypeService;
import com.xenon.core.service.LayerService;
import com.xenon.core.service.WorkspaceService;
import com.xenon.ows.wmts.GeoPackageTileService;
import com.xenon.rest.dto.FeatureTypeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for managing feature types within a datastore.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceName}/datastores/{datastoreName}/featuretypes")
@RequiredArgsConstructor
@Tag(name = "FeatureTypes", description = "Feature type (publishable resource) management")
public class FeatureTypeController {

    private final FeatureTypeService featureTypeService;
    private final DataStoreService dataStoreService;
    private final WorkspaceService workspaceService;
    private final LayerService layerService;
    private final GeoPackageTileService geoPackageTileService;

    @GetMapping
    @Operation(summary = "Get all feature types", description = "Returns all feature types in a datastore")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved feature types"),
        @ApiResponse(responseCode = "404", description = "Workspace or DataStore not found")
    })
    public ResponseEntity<FeatureTypeDto.FeatureTypeList> getAllFeatureTypes(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName,
            @Parameter(description = "Filter: 'available' for unpublished, 'configured' for published")
            @RequestParam(value = "list", required = false) String listFilter,
            HttpServletRequest request) {
        
        Workspace workspace = workspaceService.findByName(workspaceName);
        DataStore dataStore = dataStoreService.findByWorkspaceAndName(workspace.getId(), datastoreName);
        String baseUrl = getBaseUrl(request);
        
        List<FeatureTypeDto.Summary> summaries;
        
        // If requesting available (unpublished) resources, discover from datastore
        if ("available".equals(listFilter)) {
            summaries = discoverAvailableResources(dataStore, workspaceName, datastoreName, baseUrl);
        } else {
            // Return configured (published) feature types from database
            List<FeatureType> featureTypes = featureTypeService.findByDatastoreId(dataStore.getId());
            summaries = featureTypes.stream()
                    .map(ft -> FeatureTypeDto.Summary.builder()
                            .name(ft.getName())
                            .href(baseUrl + "/api/v1/workspaces/" + workspaceName + 
                                  "/datastores/" + datastoreName + "/featuretypes/" + ft.getName())
                            .build())
                    .toList();
        }
        
        return ResponseEntity.ok(FeatureTypeDto.FeatureTypeList.builder()
                .featureTypes(summaries)
                .build());
    }
    

    
    /**
     * Discover available (unpublished) resources from the datastore.
     * For SHAPEFILE type, extract the layer name from the file path.
     */
    private List<FeatureTypeDto.Summary> discoverAvailableResources(
            DataStore dataStore, String workspaceName, String datastoreName, String baseUrl) {
        
        java.util.ArrayList<FeatureTypeDto.Summary> resources = new java.util.ArrayList<>();
        
        // Get already published feature types to exclude them
        List<FeatureType> published = featureTypeService.findByDatastoreId(dataStore.getId());
        java.util.Set<String> publishedNames = published.stream()
                .map(FeatureType::getName)
                .collect(java.util.stream.Collectors.toSet());
        
        DataStoreType type = dataStore.getType();
        java.util.Map<String, Object> params = dataStore.getConnectionParams();
        
        if (DataStoreType.SHAPEFILE.equals(type) && params != null) {
            // For shapefile, the URL parameter contains the path
            Object urlObj = params.get("url");
            if (urlObj != null) {
                String url = urlObj.toString();
                // Extract filename without extension as the layer name
                String fileName = extractFileName(url);
                if (fileName != null && !publishedNames.contains(fileName)) {
                    resources.add(FeatureTypeDto.Summary.builder()
                            .name(fileName)
                            .href(baseUrl + "/api/v1/workspaces/" + workspaceName + 
                                  "/datastores/" + datastoreName + "/featuretypes/" + fileName)
                            .build());
                }
            }
        } else if (DataStoreType.GEOPACKAGE.equals(type) && params != null) {
            // 对于 GeoPackage，获取所有瓦片表作为可发布资源
            Object databaseObj = params.get("database");
            if (databaseObj != null) {
                String gpkgPath = databaseObj.toString();
                List<String> tileTables = geoPackageTileService.getTileTables(gpkgPath);
                for (String tableName : tileTables) {
                    if (!publishedNames.contains(tableName)) {
                        resources.add(FeatureTypeDto.Summary.builder()
                                .name(tableName)
                                .href(baseUrl + "/api/v1/workspaces/" + workspaceName +
                                      "/datastores/" + datastoreName + "/featuretypes/" + tableName)
                                .build());
                    }
                }
            }
        }
        
        return resources;
    }
    
    /**
     * Extract filename without extension from a file URL or path.
     */
    private String extractFileName(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // Remove file:// or file:/// prefix if present
        String path = url;
        if (path.startsWith("file:///")) {
            path = path.substring(8); // Remove file:/// (8 chars)
        } else if (path.startsWith("file://")) {
            path = path.substring(7); // Remove file:// (7 chars)
        }
        
        // Get the last part of the path
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        
        // Remove .shp extension if present
        if (fileName.toLowerCase().endsWith(".shp")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        
        return fileName;
    }

    @GetMapping("/{featureTypeName}")
    @Operation(summary = "Get feature type by name", description = "Returns a single feature type")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved feature type"),
        @ApiResponse(responseCode = "404", description = "FeatureType not found")
    })
    public ResponseEntity<FeatureTypeDto.FeatureTypeWrapper> getFeatureType(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName,
            @Parameter(description = "FeatureType name") @PathVariable String featureTypeName) {
        
        Workspace workspace = workspaceService.findByName(workspaceName);
        DataStore dataStore = dataStoreService.findByWorkspaceAndName(workspace.getId(), datastoreName);
        FeatureType featureType = featureTypeService.findByDatastoreAndName(dataStore.getId(), featureTypeName);
        
        FeatureTypeDto dto = toDto(featureType);
        
        return ResponseEntity.ok(FeatureTypeDto.FeatureTypeWrapper.builder()
                .featureType(dto)
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create feature type", description = "Creates/publishes a new feature type as a layer")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "FeatureType created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "DataStore not found")
    })
    public ResponseEntity<FeatureTypeDto.FeatureTypeWrapper> createFeatureType(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName,
            @Valid @RequestBody FeatureTypeDto.FeatureTypeWrapper request) {
        
        Workspace workspace = workspaceService.findByName(workspaceName);
        DataStore dataStore = dataStoreService.findByWorkspaceAndName(workspace.getId(), datastoreName);
        
        FeatureType featureType = toEntity(request.getFeatureType());
        featureType.setDatastoreId(dataStore.getId());
        
        FeatureType created = featureTypeService.create(featureType);
        
        // Also create a corresponding layer
        Layer layer = Layer.builder()
                .name(created.getName())
                .title(created.getTitle() != null ? created.getTitle() : created.getName())
                .description(created.getDescription())
                .type(LayerType.VECTOR)
                .enabled(true)
                .advertised(true)
                .queryable(true)
                .srs(created.getSrs())
                .featureTypeId(created.getId())
                .build();
        layerService.create(layer);
        
        log.info("Published feature type '{}' as layer in workspace '{}'", created.getName(), workspaceName);
        
        FeatureTypeDto dto = toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FeatureTypeDto.FeatureTypeWrapper.builder()
                        .featureType(dto)
                        .build());
    }

    @DeleteMapping("/{featureTypeName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete feature type", description = "Deletes a feature type")
    public ResponseEntity<Void> deleteFeatureType(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName,
            @Parameter(description = "FeatureType name") @PathVariable String featureTypeName) {
        
        Workspace workspace = workspaceService.findByName(workspaceName);
        DataStore dataStore = dataStoreService.findByWorkspaceAndName(workspace.getId(), datastoreName);
        featureTypeService.delete(dataStore.getId(), featureTypeName);
        
        return ResponseEntity.noContent().build();
    }

    private FeatureTypeDto toDto(FeatureType ft) {
        return FeatureTypeDto.builder()
                .id(ft.getId())
                .name(ft.getName())
                .nativeName(ft.getNativeName())
                .title(ft.getTitle())
                .description(ft.getDescription())
                .srs(ft.getSrs())
                .nativeSrs(ft.getNativeSrs())
                .enabled(ft.getEnabled())
                .datastoreId(ft.getDatastoreId())
                .nativeBoundingBox(ft.getNativeBboxMinX() != null ? new Double[]{
                        ft.getNativeBboxMinX(), ft.getNativeBboxMinY(),
                        ft.getNativeBboxMaxX(), ft.getNativeBboxMaxY()
                } : null)
                .latLonBoundingBox(ft.getBboxMinX() != null ? new Double[]{
                        ft.getBboxMinX(), ft.getBboxMinY(),
                        ft.getBboxMaxX(), ft.getBboxMaxY()
                } : null)
                .build();
    }

    private FeatureType toEntity(FeatureTypeDto dto) {
        return FeatureType.builder()
                .name(dto.getName())
                .nativeName(dto.getNativeName() != null ? dto.getNativeName() : dto.getName())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .srs(dto.getSrs())
                .nativeSrs(dto.getNativeSrs())
                .enabled(dto.getEnabled())
                .build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if ((scheme.equals("http") && serverPort != 80) ||
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        url.append(contextPath);
        return url.toString();
    }
}
