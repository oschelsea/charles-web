package com.xenon.rest.controller;

import com.xenon.core.entity.Layer;
import com.xenon.core.service.LayerService;
import com.xenon.core.entity.DataStore;
import com.xenon.core.entity.Workspace;
import com.xenon.core.service.DataStoreService;
import com.xenon.core.service.WorkspaceService;
import com.xenon.rest.dto.LayerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.xenon.core.util.NameValidator;

/**
 * REST API controller for managing layers.
 */
@RestController
@RequestMapping("/api/v1/layers")
@RequiredArgsConstructor
@Tag(name = "Layers", description = "Layer management operations")
public class LayerController {

    private final LayerService layerService;
    private final DataStoreService dataStoreService;
    private final WorkspaceService workspaceService;

    @GetMapping
    @Operation(summary = "Get all layers", description = "Returns a list of all published layers")
    public ResponseEntity<LayerDto.LayerList> getAllLayers(HttpServletRequest request) {
        List<Layer> layers = layerService.findAll();
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



    @GetMapping("/{name}")
    @Operation(summary = "Get layer by name", description = "Returns a single layer. Supports qualified name format: workspaceName:layerName")
    public ResponseEntity<LayerDto.LayerWrapper> getLayer(
            @Parameter(description = "Layer name or qualified name (workspaceName:layerName)") @PathVariable String name) {
        Layer layer;
        // Check if it's a qualified name (workspaceName:layerName)
        if (name.contains(":")) {
            String[] parts = name.split(":", 2);
            String workspaceName = parts[0];
            String layerName = parts[1];
            // Find layer by name (TODO: add workspace validation)
            layer = layerService.findByName(layerName);
        } else {
            layer = layerService.findByName(name);
        }
        LayerDto dto = toDto(layer);
        
        return ResponseEntity.ok(LayerDto.LayerWrapper.builder()
                .layer(dto)
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create layer", description = "Publishes a new layer")
    public ResponseEntity<LayerDto.LayerWrapper> createLayer(
            @RequestBody LayerDto.LayerWrapper request) {
        LayerDto dto = request.getLayer();
        
        // Validate layer name
        NameValidator.validateName(dto.getName(), "layer");
        
        System.out.println("Received layer create request: workspaceId=" + dto.getWorkspaceId() + ", datastoreId=" + dto.getDatastoreId() + ", name=" + dto.getName());
        
        Layer layer = toEntity(dto);
        System.out.println("Converted to entity: workspaceId=" + layer.getWorkspaceId() + ", datastoreId=" + layer.getDatastoreId());
        
        Layer created = layerService.create(layer);
        LayerDto resultDto = toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LayerDto.LayerWrapper.builder()
                        .layer(resultDto)
                        .build());
    }

    @PutMapping("/{name}")
    @Operation(summary = "Update layer", description = "Updates an existing layer. Supports qualified name format.")
    public ResponseEntity<LayerDto.LayerWrapper> updateLayer(
            @Parameter(description = "Layer name or qualified name") @PathVariable String name,
            @RequestBody LayerDto.LayerWrapper request) {
        String layerName = name.contains(":") ? name.split(":", 2)[1] : name;
        Layer updates = toEntity(request.getLayer());
        Layer updated = layerService.update(layerName, updates);
        LayerDto dto = toDto(updated);
        
        return ResponseEntity.ok(LayerDto.LayerWrapper.builder()
                .layer(dto)
                .build());
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete layer", description = "Deletes a layer. Supports qualified name format.")
    public ResponseEntity<Void> deleteLayer(
            @Parameter(description = "Layer name or qualified name") @PathVariable String name) {
        String layerName = name.contains(":") ? name.split(":", 2)[1] : name;
        layerService.delete(layerName);
        return ResponseEntity.noContent().build();
    }

    private LayerDto toDto(Layer layer) {
        String datastoreName = null;
        String workspaceName = null;
        
        if (layer.getDatastoreId() != null) {
            DataStore ds = dataStoreService.findById(layer.getDatastoreId());
            if (ds != null) {
                datastoreName = ds.getName();
                Workspace ws = workspaceService.findById(ds.getWorkspaceId());
                if (ws != null) {
                    workspaceName = ws.getName();
                }
            }
        }

        return LayerDto.builder()
                .id(layer.getId())
                .datastoreId(layer.getDatastoreId())
                .datastoreName(datastoreName)
                .workspaceName(workspaceName)
                .name(layer.getName())
                .title(layer.getTitle())
                .description(layer.getDescription())
                .type(layer.getType() != null ? layer.getType().name() : null)
                .enabled(layer.getEnabled())
                .advertised(layer.getAdvertised())
                .queryable(layer.getQueryable())
                .srs(layer.getSrs())
                .build();
    }

    private Layer toEntity(LayerDto dto) {
        return Layer.builder()
                .datastoreId(dto.getDatastoreId())
                .workspaceId(dto.getWorkspaceId())
                .name(dto.getName())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType() != null ? com.xenon.core.enums.LayerType.valueOf(dto.getType()) : null)
                .enabled(dto.getEnabled())
                .advertised(dto.getAdvertised())
                .queryable(dto.getQueryable())
                .srs(dto.getSrs())
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
