package com.xenon.rest.controller;

import com.xenon.core.entity.Workspace;
import com.xenon.core.service.WorkspaceService;
import com.xenon.rest.dto.LayerDto;
import com.xenon.rest.dto.StyleDto;
import com.xenon.rest.dto.WorkspaceDto;
import com.xenon.rest.mapper.WorkspaceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.xenon.core.util.NameValidator;

/**
 * REST API controller for managing workspaces.
 * Compatible with GeoServer REST API format.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace management operations")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final com.xenon.core.service.DataStoreService dataStoreService;
    private final com.xenon.core.service.LayerService layerService;
    private final com.xenon.core.service.StyleService styleService;
    private final com.xenon.rest.mapper.StyleRestMapper styleMapper;
    private final WorkspaceMapper workspaceMapper;

    @GetMapping
    @Operation(summary = "Get all workspaces", description = "Returns a list of all workspaces")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved workspaces")
    })
    public ResponseEntity<WorkspaceDto.WorkspaceList> getAllWorkspaces(HttpServletRequest request) {
        List<Workspace> workspaces = workspaceService.findAll();
        String baseUrl = getBaseUrl(request);
        
        List<WorkspaceDto.Summary> summaries = workspaces.stream()
                .map(ws -> workspaceMapper.toSummary(ws, baseUrl))
                .toList();
        
        return ResponseEntity.ok(WorkspaceDto.WorkspaceList.builder()
                .workspaces(summaries)
                .build());
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get workspace by name", description = "Returns a single workspace")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved workspace"),
        @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<WorkspaceDto.WorkspaceWrapper> getWorkspace(
            @Parameter(description = "Workspace name") @PathVariable String name) {
        Workspace workspace = workspaceService.findByName(name);
        WorkspaceDto dto = workspaceMapper.toDto(workspace);
        
        return ResponseEntity.ok(WorkspaceDto.WorkspaceWrapper.builder()
                .workspace(dto)
                .build());
    }

    @GetMapping("/{workspaceName}/layers")
    @Operation(summary = "Get layers by workspace", description = "Returns a list of layers in a workspace")
    public ResponseEntity<LayerDto.LayerList> getLayersByWorkspace(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            HttpServletRequest request) {
        Workspace workspace = workspaceService.findByName(workspaceName);
        List<com.xenon.core.entity.DataStore> dataStores = dataStoreService.findByWorkspaceId(workspace.getId());
        
        List<Long> dsIds = dataStores.stream()
                .map(com.xenon.core.entity.DataStore::getId)
                .toList();
        
        List<com.xenon.core.entity.Layer> layers = layerService.findByDataStoreIds(dsIds);
        String baseUrl = getBaseUrl(request);
        
        List<LayerDto.Summary> summaries = layers.stream()
                .map(layer -> LayerDto.Summary.builder()
                        .name(layer.getName())
                        .href(baseUrl + "/api/v1/layers/" + layer.getName())
                        .build())
                .toList();
        
        return ResponseEntity.ok(LayerDto.LayerList.builder()
                .layers(summaries)
                .build());
    }

    @GetMapping("/{workspaceName}/styles")
    @Operation(summary = "Get styles in workspace")
    public ResponseEntity<StyleDto.StyleList> getStylesByWorkspace(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName, 
            HttpServletRequest request) {
        
        List<com.xenon.core.entity.Style> styles = styleService.findByWorkspace(workspaceName);
        String baseUrl = getBaseUrl(request);
        
        List<StyleDto.Summary> summaries = styles.stream()
                .map(s -> styleMapper.toSummary(s, baseUrl))
                .toList();
        
        return ResponseEntity.ok(StyleDto.StyleList.builder()
                .styles(summaries)
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create workspace", description = "Creates a new workspace")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Workspace created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Workspace already exists")
    })
    public ResponseEntity<WorkspaceDto.WorkspaceWrapper> createWorkspace(
            @Valid @RequestBody WorkspaceDto.WorkspaceWrapper request) {
        // Validate workspace name
        NameValidator.validateName(request.getWorkspace().getName(), "workspace");
        
        Workspace workspace = workspaceMapper.toEntity(request.getWorkspace());
        Workspace created = workspaceService.create(workspace);
        WorkspaceDto dto = workspaceMapper.toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WorkspaceDto.WorkspaceWrapper.builder()
                        .workspace(dto)
                        .build());
    }

    @PutMapping("/{name}")
    @Operation(summary = "Update workspace", description = "Updates an existing workspace")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Workspace updated successfully"),
        @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<WorkspaceDto.WorkspaceWrapper> updateWorkspace(
            @Parameter(description = "Workspace name") @PathVariable String name,
            @Valid @RequestBody WorkspaceDto.WorkspaceWrapper request) {
        Workspace updates = workspaceMapper.toEntity(request.getWorkspace());
        Workspace updated = workspaceService.update(name, updates);
        WorkspaceDto dto = workspaceMapper.toDto(updated);
        
        return ResponseEntity.ok(WorkspaceDto.WorkspaceWrapper.builder()
                .workspace(dto)
                .build());
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete workspace", description = "Deletes a workspace")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Workspace deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<Void> deleteWorkspace(
            @Parameter(description = "Workspace name") @PathVariable String name) {
        workspaceService.delete(name);
        return ResponseEntity.noContent().build();
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
