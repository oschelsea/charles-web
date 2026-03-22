package com.xenon.tiles3d;

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
 * 3D Tiles REST 控制器。
 * 提供 3D Tiles 瓦片集和瓦片内容的端点。
 * URL 格式: /services/{workspace}:{layerName}/3dtiles
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/xenon/services")
@Tag(name = "3D Tiles", description = "3D Tiles Service")
public class Tiles3DController {

    private final Tiles3DService tiles3dService;
    private final DataStoreService dataStoreService;
    private final WorkspaceService workspaceService;

    /**
     * 获取 3D Tiles tileset.json。
     * URL: /services/{workspace}:{layerName}/3dtiles/tileset.json
     */
    @GetMapping("/{qualifiedLayer}/3dtiles/tileset.json")
    @Operation(summary = "获取 Tileset", description = "获取 3D Tiles tileset.json 元数据")
    public ResponseEntity<String> getTileset(
            @Parameter(description = "限定图层名 (workspace:layerName)") @PathVariable String qualifiedLayer,
            HttpServletRequest request
    ) {
        // 查找对应的数据存储
        DataStore dataStore = findTiles3DDataStore(qualifiedLayer);

        if (dataStore != null) {
            // 从缓存加载
            try {
                String tilesetPath = getTilesetPath(dataStore);
                String tilesetJson = tiles3dService.getTilesetFromCache(tilesetPath);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                        .body(tilesetJson);
            } catch (IOException e) {
                log.error("从缓存加载 tileset 失败: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // 回退到动态生成
        String baseUrl = getBaseUrl(request);
        String tilesetJson = tiles3dService.getTileset(qualifiedLayer, baseUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(tilesetJson);
    }

    /**
     * 获取 3D Tiles 瓦片内容 (b3dm, i3dm, pnts, glb 等)。
     * URL: /services/{workspace}:{layerName}/3dtiles/{*path}
     */
    @GetMapping(value = "/{qualifiedLayer}/3dtiles/{*path}", produces = "application/octet-stream")
    @Operation(summary = "获取瓦片", description = "获取 3D 瓦片 (b3dm, i3dm, pnts, glb)")
    public ResponseEntity<byte[]> getTile(
            @Parameter(description = "限定图层名 (workspace:layerName)") @PathVariable String qualifiedLayer,
            @PathVariable String path,
            HttpServletRequest request
    ) {
        if (path == null || path.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 移除开头的斜杠
        String tilePath = path.startsWith("/") ? path.substring(1) : path;

        // 查找对应的数据存储
        DataStore dataStore = findTiles3DDataStore(qualifiedLayer);

        if (dataStore != null) {
            // 从缓存加载
            try {
                String tilesetPath = getTilesetPath(dataStore);
                String basePath = tiles3dService.validateCachePath(tilesetPath).toString();
                byte[] content = tiles3dService.getTileFromCache(basePath, tilePath);
                String contentType = getContentType(tilePath);
                return createTileResponse(content, contentType);
            } catch (IOException e) {
                log.error("从缓存加载瓦片失败: {}", e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }

        // Fallback to dynamic generation
        String extension = tilePath.substring(tilePath.lastIndexOf('.')).toLowerCase();

        return switch (extension) {
            case ".b3dm" -> {
                byte[] content = tiles3dService.getB3dmTile(qualifiedLayer, tilePath);
                yield createTileResponse(content, "application/octet-stream");
            }
            case ".i3dm" -> {
                byte[] content = tiles3dService.getI3dmTile(qualifiedLayer, tilePath);
                yield createTileResponse(content, "application/octet-stream");
            }
            case ".pnts" -> {
                byte[] content = tiles3dService.getPntsTile(qualifiedLayer, tilePath);
                yield createTileResponse(content, "application/octet-stream");
            }
            case ".glb" -> {
                yield createGlbResponse();
            }
            case ".gltf" -> {
                yield createGltfResponse();
            }
            case ".json" -> {
                // Handle nested tileset.json references
                yield createJsonResponse(tilePath, qualifiedLayer);
            }
            default -> ResponseEntity.notFound().build();
        };
    }

    /**
     * Get content type based on file extension.
     */
    private String getContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".b3dm") || lower.endsWith(".i3dm") || lower.endsWith(".pnts") || lower.endsWith(".cmpt")) {
            return "application/octet-stream";
        } else if (lower.endsWith(".glb")) {
            return "model/gltf-binary";
        } else if (lower.endsWith(".gltf") || lower.endsWith(".json")) {
            return "application/json";
        }
        return "application/octet-stream";
    }

    /**
     * Handle JSON file response (for nested tileset references).
     */
    private ResponseEntity<byte[]> createJsonResponse(String path, String tileset) {
        // For now, return not found - this handles the case of nested tileset.json
        log.debug("JSON tile request for path: {} in tileset: {}", path, tileset);
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<byte[]> createGlbResponse() {
        // Return minimal glb (binary glTF)
        // GLB format: magic (4) + version (4) + length (4) + chunks
        String json = "{\"asset\":{\"version\":\"2.0\"},\"scene\":0,\"scenes\":[{\"nodes\":[]}]}";
        byte[] jsonBytes = json.getBytes();

        // Pad to 4-byte boundary
        int jsonPadding = (4 - (jsonBytes.length % 4)) % 4;
        int jsonChunkLength = jsonBytes.length + jsonPadding;
        int totalLength = 12 + 8 + jsonChunkLength;

        byte[] glb = new byte[totalLength];
        int offset = 0;

        // Magic "glTF"
        glb[offset++] = 'g';
        glb[offset++] = 'l';
        glb[offset++] = 'T';
        glb[offset++] = 'F';

        // Version 2
        writeUint32LE(glb, offset, 2);
        offset += 4;

        // Total length
        writeUint32LE(glb, offset, totalLength);
        offset += 4;

        // JSON chunk length
        writeUint32LE(glb, offset, jsonChunkLength);
        offset += 4;

        // JSON chunk type
        glb[offset++] = 'J';
        glb[offset++] = 'S';
        glb[offset++] = 'O';
        glb[offset++] = 'N';

        // JSON content
        System.arraycopy(jsonBytes, 0, glb, offset, jsonBytes.length);

        return createTileResponse(glb, "model/gltf-binary");
    }

    private ResponseEntity<byte[]> createGltfResponse() {
        String gltf = """
                {
                  "asset": {"version": "2.0"},
                  "scene": 0,
                  "scenes": [{"nodes": []}],
                  "nodes": []
                }
                """;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("model/gltf+json"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .body(gltf.getBytes());
    }

    private ResponseEntity<byte[]> createTileResponse(byte[] content, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    private String extractTilePath(HttpServletRequest request, String tileset, String extension) {
        String uri = request.getRequestURI();
        String prefix = "/3dtiles/" + tileset + "/";
        if (uri.startsWith(prefix)) {
            return uri.substring(prefix.length());
        }
        return uri;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        return url.toString();
    }

    private void writeUint32LE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    /**
     * Find a 3D Tiles DataStore by name.
     * Supports qualified name format: workspace:storeName (like GeoServer)
     */
    private DataStore findTiles3DDataStore(String qualifiedName) {
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
                        if (ds.getName().equals(storeName) && ds.getType() == DataStoreType.TILES3D_CACHE) {
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
                        if (ds.getName().equals(storeName) && ds.getType() == DataStoreType.TILES3D_CACHE) {
                            return ds;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error finding DataStore: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get tileset.json path from DataStore connection parameters.
     */
    private String getTilesetPath(DataStore dataStore) {
        Map<String, Object> params = dataStore.getConnectionParams();
        if (params != null && params.containsKey("tilesetPath")) {
            return params.get("tilesetPath").toString();
        }
        throw new IllegalStateException("DataStore missing tilesetPath parameter");
    }
}
