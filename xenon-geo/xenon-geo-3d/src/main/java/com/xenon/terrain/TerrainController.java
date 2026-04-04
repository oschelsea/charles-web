package com.xenon.terrain;

import com.xenon.core.entity.DataStore;
import com.xenon.core.enums.DataStoreType;
import com.xenon.core.service.DataStoreService;
import com.xenon.core.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Cesium Terrain REST Controller.
 * Provides Quantized Mesh terrain endpoints.
 * URL format: /services/{workspace}:{layerName}/terrain
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/xenon/services")
@Tag(name = "Terrain", description = "Cesium Terrain Service")
public class TerrainController {

    private final TerrainService terrainService;
    private final DataStoreService dataStoreService;
    private final WorkspaceService workspaceService;

    /**
     * Get layer.json metadata for terrain.
     * URL: /services/{workspace}:{layerName}/terrain/layer.json
     */
    @GetMapping("/{qualifiedLayer}/terrain/layer.json")
    @Operation(summary = "Get layer.json", description = "Get Cesium terrain metadata (layer.json)")
    public ResponseEntity<String> getLayerJson(
            @Parameter(description = "Qualified layer name (workspace:layerName)") @PathVariable String qualifiedLayer,
            HttpServletRequest request
    ) {
        log.info("Terrain layer.json request: {}", qualifiedLayer);

        // Find the corresponding TERRAIN_CACHE DataStore
        DataStore dataStore = findTerrainDataStore(qualifiedLayer);

        if (dataStore == null) {
            log.warn("No TERRAIN_CACHE DataStore found for: {}", qualifiedLayer);
            return ResponseEntity.notFound().build();
        }

        try {
            String layerJsonPath = getLayerJsonPath(dataStore);
            String layerJson = terrainService.getLayerJson(layerJsonPath, qualifiedLayer);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                    .body(layerJson);
        } catch (IOException e) {
            log.error("Failed to load layer.json: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Get terrain tile (.terrain file).
     * URL: /services/{workspace}:{layerName}/terrain/{z}/{x}/{y}.terrain
     */
    @GetMapping(value = "/{qualifiedLayer}/terrain/{z}/{x}/{y}.terrain",
            produces = "application/vnd.quantized-mesh")
    @Operation(summary = "Get terrain tile", description = "Get quantized mesh terrain tile")
    public ResponseEntity<byte[]> getTerrainTile(
            @Parameter(description = "Qualified layer name (workspace:layerName)") @PathVariable String qualifiedLayer,
            @Parameter(description = "Zoom level") @PathVariable int z,
            @Parameter(description = "Tile column (X)") @PathVariable int x,
            @Parameter(description = "Tile row (Y)") @PathVariable int y,
            HttpServletRequest request
    ) {
        log.debug("Terrain tile request: {} z={}, x={}, y={}", qualifiedLayer, z, x, y);

        // Find the corresponding TERRAIN_CACHE DataStore
        DataStore dataStore = findTerrainDataStore(qualifiedLayer);

        if (dataStore == null) {
            log.warn("No TERRAIN_CACHE DataStore found for: {}", qualifiedLayer);
            return ResponseEntity.notFound().build();
        }

        try {
            String layerJsonPath = getLayerJsonPath(dataStore);
            String basePath = terrainService.validateCachePath(layerJsonPath).toString();
            byte[] content = terrainService.getTerrainTile(basePath, z, x, y);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.quantized-mesh"));

            // Check if tiles are gzip compressed from DataStore connectionParams
            boolean zipped = isZipped(dataStore, basePath);
            if (zipped) {
                headers.add("Content-Encoding", "gzip");
            }

            headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
            headers.setContentLength(content.length);

            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.debug("Terrain tile not found: z={}, x={}, y={}", z, x, y);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get terrain metadata info from meta.json.
     * Used when creating a TERRAIN_CACHE datastore to auto-fill parameters.
     *
     * @param layerJsonPath Path to layer.json file
     */
    @GetMapping("/terrain/meta")
    @Operation(summary = "Get terrain meta info", description = "Read meta.json from terrain cache directory")
    public ResponseEntity<Map<String, Object>> getTerrainMeta(
            @Parameter(description = "Path to layer.json") jakarta.servlet.http.HttpServletRequest request
    ) {
        String layerJsonPath = request.getParameter("layerJsonPath");
        if (layerJsonPath == null || layerJsonPath.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String basePath = terrainService.validateCachePath(layerJsonPath).toString();
            Map<String, Object> metaInfo = terrainService.getMetaInfo(basePath);
            return ResponseEntity.ok(metaInfo);
        } catch (IOException e) {
            log.debug("Failed to read terrain meta: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Find a TERRAIN_CACHE DataStore by qualified name.
     * Supports qualified name format: workspace:storeName
     */
    private DataStore findTerrainDataStore(String qualifiedName) {
        try {
            String workspaceName = null;
            String storeName = qualifiedName;

            // Parse qualified name format: workspace:storeName
            if (qualifiedName.contains(":")) {
                String[] parts = qualifiedName.split(":", 2);
                workspaceName = parts[0];
                storeName = parts[1];
            }

            if (workspaceName != null) {
                // Search in specific workspace
                var workspace = workspaceService.findByName(workspaceName);
                if (workspace != null) {
                    List<DataStore> dataStores = dataStoreService.findByWorkspaceId(workspace.getId());
                    for (DataStore ds : dataStores) {
                        if (ds.getName().equals(storeName) && ds.getType() == DataStoreType.TERRAIN_CACHE) {
                            return ds;
                        }
                    }
                }
            } else {
                // Search across all workspaces
                var workspaces = workspaceService.list();
                for (var workspace : workspaces) {
                    List<DataStore> dataStores = dataStoreService.findByWorkspaceId(workspace.getId());
                    for (DataStore ds : dataStores) {
                        if (ds.getName().equals(storeName) && ds.getType() == DataStoreType.TERRAIN_CACHE) {
                            return ds;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error finding TERRAIN_CACHE DataStore: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get layer.json path from DataStore connection parameters.
     */
    private String getLayerJsonPath(DataStore dataStore) {
        Map<String, Object> params = dataStore.getConnectionParams();
        if (params != null && params.containsKey("layerJsonPath")) {
            return params.get("layerJsonPath").toString();
        }
        throw new IllegalStateException("DataStore missing layerJsonPath parameter");
    }

    /**
     * Check if terrain tiles are gzip compressed.
     * First checks DataStore connectionParams for "zipped" field,
     * falls back to reading meta.json if not found.
     *
     * @param dataStore The terrain data store
     * @param basePath  Base directory path (for fallback meta.json reading)
     * @return true if tiles are gzip compressed, false otherwise
     */
    private boolean isZipped(DataStore dataStore, String basePath) {
        // First try to get from connectionParams
        Map<String, Object> params = dataStore.getConnectionParams();
        if (params != null && params.containsKey("zipped")) {
            Object value = params.get("zipped");
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }

        // Fallback to reading meta.json
        boolean zipped = terrainService.isZipped(basePath);
        if (params != null) {
            params.put("zipped", zipped);
        }
        return zipped;
    }
}
