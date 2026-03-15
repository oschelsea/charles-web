package com.xenon.tiles3d;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 3D Tiles Service implementation.
 * Provides 3D Tiles 1.0/1.1 format support.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Tiles3DService {

    /**
     * Get tileset.json metadata for a 3D tileset.
     * 
     * @param tilesetName Tileset name
     * @param baseUrl Base URL for tile requests
     * @return Tileset JSON
     */
    public String getTileset(String tilesetName, String baseUrl) {
        log.debug("3D Tiles tileset request: {}", tilesetName);
        
        // Generate a sample tileset.json
        // In production, this would be loaded from file or generated from 3D data
        return String.format("""
            {
              "asset": {
                "version": "1.0",
                "tilesetVersion": "1.0.0"
              },
              "geometricError": 500,
              "root": {
                "boundingVolume": {
                  "region": [
                    -3.141592653589793,
                    -1.5707963267948966,
                    3.141592653589793,
                    1.5707963267948966,
                    0,
                    1000
                  ]
                },
                "geometricError": 100,
                "refine": "ADD",
                "content": {
                  "uri": "%s/3dtiles/%s/root.b3dm"
                },
                "children": []
              }
            }
            """, baseUrl, tilesetName);
    }

    /**
     * Get tileset.json from cache directory.
     * 
     * @param tilesetPath Absolute path to tileset.json
     * @return Tileset JSON content
     * @throws IOException if file cannot be read
     */
    public String getTilesetFromCache(String tilesetPath) throws IOException {
        log.debug("Loading tileset from cache: {}", tilesetPath);
        
        Path path = Paths.get(tilesetPath);
        if (!Files.exists(path)) {
            throw new IOException("Tileset file not found: " + tilesetPath);
        }
        
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Get a tile file from cache directory.
     * 
     * @param basePath Base path (directory containing tileset.json)
     * @param tilePath Relative path to tile within the cache
     * @return Tile file content as bytes
     * @throws IOException if file cannot be read
     */
    public byte[] getTileFromCache(String basePath, String tilePath) throws IOException {
        log.debug("Loading tile from cache: base={}, tile={}", basePath, tilePath);
        
        // Validate the tile path to prevent directory traversal
        Path base = Paths.get(basePath).toAbsolutePath().normalize();
        Path tile = base.resolve(tilePath).normalize();
        
        if (!tile.startsWith(base)) {
            throw new IOException("Invalid tile path: path traversal detected");
        }
        
        if (!Files.exists(tile)) {
            throw new IOException("Tile file not found: " + tile);
        }
        
        return Files.readAllBytes(tile);
    }

    /**
     * Validate cache path and return the base directory.
     * 
     * @param tilesetPath Path to tileset.json
     * @return Base directory path
     * @throws IOException if path is invalid
     */
    public Path validateCachePath(String tilesetPath) throws IOException {
        Path path = Paths.get(tilesetPath).toAbsolutePath().normalize();
        
        if (!Files.exists(path)) {
            throw new IOException("Tileset file not found: " + tilesetPath);
        }
        
        if (!path.getFileName().toString().toLowerCase().endsWith(".json")) {
            throw new IOException("Invalid tileset file: must be a .json file");
        }
        
        return path.getParent();
    }

    /**
     * Get a B3DM (Batched 3D Model) tile.
     * 
     * @param tilesetName Tileset name
     * @param tilePath Tile path within the tileset
     * @return B3DM tile data
     */
    public byte[] getB3dmTile(String tilesetName, String tilePath) {
        log.debug("B3DM tile request: tileset={}, path={}", tilesetName, tilePath);
        
        // TODO: Implement actual B3DM tile serving
        // For now, return a minimal valid B3DM structure
        return createEmptyB3dm();
    }

    /**
     * Get an I3DM (Instanced 3D Model) tile.
     * 
     * @param tilesetName Tileset name
     * @param tilePath Tile path within the tileset
     * @return I3DM tile data
     */
    public byte[] getI3dmTile(String tilesetName, String tilePath) {
        log.debug("I3DM tile request: tileset={}, path={}", tilesetName, tilePath);
        
        // TODO: Implement actual I3DM tile serving
        return createEmptyI3dm();
    }

    /**
     * Get a PNTS (Point Cloud) tile.
     * 
     * @param tilesetName Tileset name
     * @param tilePath Tile path within the tileset
     * @return PNTS tile data
     */
    public byte[] getPntsTile(String tilesetName, String tilePath) {
        log.debug("PNTS tile request: tileset={}, path={}", tilesetName, tilePath);
        
        // TODO: Implement actual PNTS tile serving
        return createEmptyPnts();
    }

    /**
     * Create an empty B3DM tile.
     * B3DM format: magic (4) + version (4) + byteLength (4) + featureTableJsonLength (4) 
     *              + featureTableBinaryLength (4) + batchTableJsonLength (4) + batchTableBinaryLength (4)
     *              + featureTableJson + featureTableBinary + batchTableJson + batchTableBinary + glTF
     */
    private byte[] createEmptyB3dm() {
        String featureTableJson = "{}";
        byte[] featureTableBytes = padTo8Bytes(featureTableJson.getBytes(StandardCharsets.UTF_8));
        
        // Minimal glTF with empty scene
        String gltfJson = """
            {"asset":{"version":"2.0"},"scene":0,"scenes":[{"nodes":[]}],"nodes":[]}
            """;
        byte[] gltfBytes = padTo8Bytes(gltfJson.getBytes(StandardCharsets.UTF_8));
        
        int headerSize = 28;
        int totalSize = headerSize + featureTableBytes.length + gltfBytes.length;
        
        byte[] result = new byte[totalSize];
        int offset = 0;
        
        // Magic "b3dm"
        result[offset++] = 'b';
        result[offset++] = '3';
        result[offset++] = 'd';
        result[offset++] = 'm';
        
        // Version (1)
        writeUint32LE(result, offset, 1);
        offset += 4;
        
        // Byte length
        writeUint32LE(result, offset, totalSize);
        offset += 4;
        
        // Feature table JSON length
        writeUint32LE(result, offset, featureTableBytes.length);
        offset += 4;
        
        // Feature table binary length
        writeUint32LE(result, offset, 0);
        offset += 4;
        
        // Batch table JSON length
        writeUint32LE(result, offset, 0);
        offset += 4;
        
        // Batch table binary length
        writeUint32LE(result, offset, 0);
        offset += 4;
        
        // Feature table JSON
        System.arraycopy(featureTableBytes, 0, result, offset, featureTableBytes.length);
        offset += featureTableBytes.length;
        
        // glTF
        System.arraycopy(gltfBytes, 0, result, offset, gltfBytes.length);
        
        return result;
    }

    /**
     * Create an empty I3DM tile.
     */
    private byte[] createEmptyI3dm() {
        // Simplified I3DM with just header
        byte[] result = new byte[32];
        
        result[0] = 'i';
        result[1] = '3';
        result[2] = 'd';
        result[3] = 'm';
        
        writeUint32LE(result, 4, 1); // version
        writeUint32LE(result, 8, 32); // byte length
        
        return result;
    }

    /**
     * Create an empty PNTS tile.
     */
    private byte[] createEmptyPnts() {
        String featureTableJson = "{\"POINTS_LENGTH\":0}";
        byte[] featureTableBytes = padTo8Bytes(featureTableJson.getBytes(StandardCharsets.UTF_8));
        
        int headerSize = 28;
        int totalSize = headerSize + featureTableBytes.length;
        
        byte[] result = new byte[totalSize];
        int offset = 0;
        
        result[offset++] = 'p';
        result[offset++] = 'n';
        result[offset++] = 't';
        result[offset++] = 's';
        
        writeUint32LE(result, offset, 1);
        offset += 4;
        
        writeUint32LE(result, offset, totalSize);
        offset += 4;
        
        writeUint32LE(result, offset, featureTableBytes.length);
        offset += 4;
        
        writeUint32LE(result, offset, 0);
        offset += 4;
        
        writeUint32LE(result, offset, 0);
        offset += 4;
        
        writeUint32LE(result, offset, 0);
        offset += 4;
        
        System.arraycopy(featureTableBytes, 0, result, offset, featureTableBytes.length);
        
        return result;
    }

    private byte[] padTo8Bytes(byte[] data) {
        int padding = (8 - (data.length % 8)) % 8;
        if (padding == 0) return data;
        
        byte[] result = new byte[data.length + padding];
        System.arraycopy(data, 0, result, 0, data.length);
        for (int i = data.length; i < result.length; i++) {
            result[i] = ' ';
        }
        return result;
    }

    private void writeUint32LE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }
}
