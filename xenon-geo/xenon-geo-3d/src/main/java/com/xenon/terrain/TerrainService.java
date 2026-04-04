package com.xenon.terrain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Cesium Terrain Service implementation.
 * Provides Quantized Mesh terrain format support.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TerrainService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get layer.json metadata from cache directory.
     * Rewrites the tiles URL to point to the service endpoint.
     *
     * @param layerJsonPath   Absolute path to layer.json
     * @param qualifiedLayer  Qualified layer name (workspace:layerName)
     * @return Modified layer.json with correct tile URLs
     * @throws IOException if file cannot be read
     */
    public String getLayerJson(String layerJsonPath, String qualifiedLayer) throws IOException {
        log.debug("Loading layer.json from: {}", layerJsonPath);

        Path path = Paths.get(layerJsonPath);
        if (!Files.exists(path)) {
            throw new IOException("layer.json file not found: " + layerJsonPath);
        }

        String content = Files.readString(path, StandardCharsets.UTF_8);

        // Rewrite tiles URL to point to our service
        return rewriteLayerJson(content, qualifiedLayer);
    }

    /**
     * Rewrite the tiles array in layer.json to point to our service endpoint.
     *
     * @param originalJson    Original layer.json content
     * @param qualifiedLayer  Qualified layer name
     * @return Modified JSON string
     */
    private String rewriteLayerJson(String originalJson, String qualifiedLayer) {
        try {
            JsonNode json = objectMapper.readTree(originalJson);

            // Build the service URL for tiles
            String serviceUrl = "/xenon/services/" + qualifiedLayer + "/terrain";

            // Rewrite tiles array
            ArrayNode tiles = ((ObjectNode) json).putArray("tiles");
            tiles.add(serviceUrl + "/{z}/{x}/{y}.terrain");

            // Add metadata for client reference if not present
            if (!json.has("metadata")) {
                ((ObjectNode) json).put("metadata", serviceUrl + "/layer.json");
            }

            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            log.warn("Failed to rewrite layer.json, returning original: {}", e.getMessage());
            return originalJson;
        }
    }

    /**
     * Get terrain tile from cache.
     *
     * @param basePath Base directory containing layer.json
     * @param z        Zoom level
     * @param x        Tile column (X)
     * @param y        Tile row (Y)
     * @return .terrain file content as bytes
     * @throws IOException if file cannot be read
     */
    public byte[] getTerrainTile(String basePath, int z, int x, int y) throws IOException {
        log.debug("Loading terrain tile: z={}, x={}, y={}", z, x, y);

        // Construct tile path: {basePath}/{z}/{x}/{y}.terrain
        Path base = Paths.get(basePath).toAbsolutePath().normalize();
        Path tile = base.resolve(String.format("%d/%d/%d.terrain", z, x, y)).normalize();

        // Validate path to prevent directory traversal
        if (!tile.startsWith(base)) {
            throw new IOException("Invalid tile path: path traversal detected");
        }

        if (!Files.exists(tile)) {
            throw new IOException("Terrain tile not found: " + tile);
        }

        return Files.readAllBytes(tile);
    }

    /**
     * Validate cache path and return the base directory.
     *
     * @param layerJsonPath Path to layer.json
     * @return Base directory path
     * @throws IOException if path is invalid
     */
    public Path validateCachePath(String layerJsonPath) throws IOException {
        Path path = Paths.get(layerJsonPath).toAbsolutePath().normalize();

        if (!Files.exists(path)) {
            throw new IOException("layer.json file not found: " + layerJsonPath);
        }

        String fileName = path.getFileName().toString().toLowerCase();
        if (!fileName.equals("layer.json") && !fileName.endsWith(".json")) {
            throw new IOException("Invalid terrain metadata file: expected layer.json");
        }

        return path.getParent();
    }

    /**
     * Check if terrain tiles are gzip compressed by reading meta.json.
     *
     * @param basePath Base directory containing meta.json
     * @return true if tiles are gzip compressed, false otherwise (default: false)
     */
    public boolean isZipped(String basePath) {
        try {
            Path metaPath = Paths.get(basePath, "meta.json");
            if (!Files.exists(metaPath)) {
                log.debug("meta.json not found at {}, defaulting to zipped=false", basePath);
                return false;
            }

            String content = Files.readString(metaPath, StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(content);

            // Check "ziped" field (note: the field name is intentionally "ziped" not "zipped")
            if (json.has("ziped")) {
                boolean zipped = json.get("ziped").asBoolean(false);
                log.debug("meta.json ziped field: {}", zipped);
                return zipped;
            }

            // Default to false if field not present
            return false;
        } catch (Exception e) {
            log.warn("Failed to read meta.json: {}, defaulting to zipped=false", e.getMessage());
            return false;
        }
    }

    /**
     * Get terrain metadata info from meta.json.
     * Returns key fields for auto-filling datastore parameters.
     *
     * @param basePath Base directory containing meta.json
     * @return Map with metadata fields (zipped, bounds, minzoom, maxzoom, etc.)
     */
    public Map<String, Object> getMetaInfo(String basePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("zipped", false); // default

        try {
            Path metaPath = Paths.get(basePath, "meta.json");
            if (!Files.exists(metaPath)) {
                log.debug("meta.json not found at {}", basePath);
                return result;
            }

            String content = Files.readString(metaPath, StandardCharsets.UTF_8);
            JsonNode json = objectMapper.readTree(content);

            // Extract key fields
            if (json.has("ziped")) {
                result.put("zipped", json.get("ziped").asBoolean(false));
            }
            if (json.has("bounds")) {
                JsonNode bounds = json.get("bounds");
                if (bounds.isArray() && bounds.size() >= 4) {
                    Map<String, Double> boundsMap = new HashMap<>();
                    boundsMap.put("west", bounds.get(0).asDouble());
                    boundsMap.put("south", bounds.get(1).asDouble());
                    boundsMap.put("east", bounds.get(2).asDouble());
                    boundsMap.put("north", bounds.get(3).asDouble());
                    result.put("bounds", boundsMap);
                }
            }
            if (json.has("minzoom")) {
                result.put("minzoom", json.get("minzoom").asInt());
            }
            if (json.has("maxzoom")) {
                result.put("maxzoom", json.get("maxzoom").asInt());
            }
            if (json.has("proj")) {
                result.put("proj", json.get("proj").asInt());
            }
            if (json.has("tiletrans")) {
                result.put("tiletrans", json.get("tiletrans").asText());
            }

            log.debug("Loaded terrain meta info: {}", result);
        } catch (Exception e) {
            log.warn("Failed to read meta.json: {}", e.getMessage());
        }

        return result;
    }
}
