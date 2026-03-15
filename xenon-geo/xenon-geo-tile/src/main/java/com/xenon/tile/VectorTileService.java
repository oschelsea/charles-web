package com.xenon.tile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Vector Tile (MVT/PBF) Service implementation.
 * Provides vector tiles in Mapbox Vector Tile format.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorTileService {

    private static final int TILE_EXTENT = 4096;

    /**
     * Get a vector tile for the specified layer.
     * 
     * @param layer Layer name
     * @param z Zoom level
     * @param x Column (X coordinate)
     * @param y Row (Y coordinate)
     * @return Vector tile as byte array (MVT/PBF format, gzip compressed)
     */
    public byte[] getTile(String layer, int z, int x, int y) throws IOException {
        log.debug("Vector tile request: layer={}, z={}, x={}, y={}", layer, z, x, y);
        
        // TODO: Implement actual MVT generation using GeoTools or java-vector-tile
        // For now, return an empty MVT tile
        return generateEmptyTile(layer);
    }

    /**
     * Get tile bounds in WGS84 coordinates.
     */
    public double[] getTileBounds(int z, int x, int y) {
        double n = Math.pow(2.0, z);
        double lonMin = x / n * 360.0 - 180.0;
        double lonMax = (x + 1) / n * 360.0 - 180.0;
        double latMinRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * (y + 1) / n)));
        double latMaxRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n)));
        double latMin = Math.toDegrees(latMinRad);
        double latMax = Math.toDegrees(latMaxRad);
        
        return new double[] { lonMin, latMin, lonMax, latMax };
    }

    /**
     * Generate an empty MVT tile.
     * MVT format is Protocol Buffer based, this returns a minimal valid tile.
     */
    private byte[] generateEmptyTile(String layerName) throws IOException {
        // Minimal valid MVT tile (empty layer)
        // This is a simplified protobuf encoding for an empty layer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Layer message (field 3 in Tile message)
        byte[] nameBytes = (layerName != null ? layerName : "default").getBytes(StandardCharsets.UTF_8);
        
        // Write layer
        baos.write(0x1A); // Field 3, wire type 2 (length-delimited)
        
        // Calculate layer content size
        ByteArrayOutputStream layerContent = new ByteArrayOutputStream();
        // Name (field 1)
        layerContent.write(0x0A); // Field 1, wire type 2
        writeVarint(layerContent, nameBytes.length);
        layerContent.write(nameBytes);
        // Version (field 15)
        layerContent.write(0x78); // Field 15, wire type 0
        layerContent.write(0x02); // Version 2
        // Extent (field 5)
        layerContent.write(0x28); // Field 5, wire type 0
        writeVarint(layerContent, TILE_EXTENT);
        
        byte[] layerBytes = layerContent.toByteArray();
        writeVarint(baos, layerBytes.length);
        baos.write(layerBytes);
        
        // Compress with gzip
        byte[] uncompressed = baos.toByteArray();
        return gzipCompress(uncompressed);
    }

    /**
     * Write a varint to the output stream.
     */
    private void writeVarint(ByteArrayOutputStream out, int value) {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value);
    }

    /**
     * Gzip compress the data.
     */
    private byte[] gzipCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }
}
