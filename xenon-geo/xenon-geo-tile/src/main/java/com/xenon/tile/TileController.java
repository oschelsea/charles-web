package com.xenon.tile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Tile service REST controller.
 * Provides XYZ/TMS raster tiles and MVT vector tiles.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Tiles", description = "XYZ/TMS and Vector Tile Services")
public class TileController {

    private final XyzTileService xyzTileService;
    private final VectorTileService vectorTileService;

    // ==================== XYZ Raster Tiles ====================

    /**
     * Get XYZ raster tile.
     * URL pattern: /tiles/{layer}/{z}/{x}/{y}.{format}
     */
    @GetMapping("/tiles/{layer}/{z}/{x}/{y}.{format}")
    @Operation(summary = "Get XYZ Tile", description = "Get a raster tile in XYZ scheme")
    public ResponseEntity<byte[]> getXyzTile(
            @Parameter(description = "Layer name") @PathVariable String layer,
            @Parameter(description = "Zoom level") @PathVariable int z,
            @Parameter(description = "Column (X)") @PathVariable int x,
            @Parameter(description = "Row (Y)") @PathVariable int y,
            @Parameter(description = "Format (png, jpg)") @PathVariable String format
    ) throws IOException {
        byte[] tile = xyzTileService.getTile(layer, z, x, y, format, false);
        return createTileResponse(tile, format);
    }

    /**
     * Get TMS raster tile (Y-axis flipped).
     * URL pattern: /tms/{layer}/{z}/{x}/{y}.{format}
     */
    @GetMapping("/tms/{layer}/{z}/{x}/{y}.{format}")
    @Operation(summary = "Get TMS Tile", description = "Get a raster tile in TMS scheme (Y-flipped)")
    public ResponseEntity<byte[]> getTmsTile(
            @Parameter(description = "Layer name") @PathVariable String layer,
            @Parameter(description = "Zoom level") @PathVariable int z,
            @Parameter(description = "Column (X)") @PathVariable int x,
            @Parameter(description = "Row (Y, TMS)") @PathVariable int y,
            @Parameter(description = "Format (png, jpg)") @PathVariable String format
    ) throws IOException {
        byte[] tile = xyzTileService.getTile(layer, z, x, y, format, true);
        return createTileResponse(tile, format);
    }

    /**
     * Alternative XYZ endpoint with format as query param.
     * URL pattern: /xyz/{layer}/{z}/{x}/{y}?format=png
     */
    @GetMapping("/xyz/{layer}/{z}/{x}/{y}")
    @Operation(summary = "Get XYZ Tile (alt)", description = "Alternative XYZ endpoint with format as query param")
    public ResponseEntity<byte[]> getXyzTileAlt(
            @PathVariable String layer,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y,
            @RequestParam(defaultValue = "png") String format
    ) throws IOException {
        byte[] tile = xyzTileService.getTile(layer, z, x, y, format, false);
        return createTileResponse(tile, format);
    }

    // ==================== Vector Tiles (MVT) ====================

    /**
     * Get MVT vector tile.
     * URL pattern: /mvt/{layer}/{z}/{x}/{y}.pbf
     */
    @GetMapping("/mvt/{layer}/{z}/{x}/{y}.pbf")
    @Operation(summary = "Get MVT Tile", description = "Get a vector tile in Mapbox Vector Tile format")
    public ResponseEntity<byte[]> getMvtTile(
            @Parameter(description = "Layer name") @PathVariable String layer,
            @Parameter(description = "Zoom level") @PathVariable int z,
            @Parameter(description = "Column (X)") @PathVariable int x,
            @Parameter(description = "Row (Y)") @PathVariable int y
    ) throws IOException {
        byte[] tile = vectorTileService.getTile(layer, z, x, y);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.mapbox-vector-tile"));
        headers.add("Content-Encoding", "gzip");
        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        headers.setContentLength(tile.length);
        
        return new ResponseEntity<>(tile, headers, HttpStatus.OK);
    }

    /**
     * Get MVT tile with alternative extension.
     * URL pattern: /mvt/{layer}/{z}/{x}/{y}.mvt
     */
    @GetMapping("/mvt/{layer}/{z}/{x}/{y}.mvt")
    @Operation(summary = "Get MVT Tile (alt)", description = "Alternative MVT endpoint with .mvt extension")
    public ResponseEntity<byte[]> getMvtTileAlt(
            @PathVariable String layer,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y
    ) throws IOException {
        return getMvtTile(layer, z, x, y);
    }

    // ==================== TileJSON Metadata ====================

    /**
     * Get TileJSON metadata for a layer.
     * URL pattern: /tiles/{layer}/tilejson.json
     */
    @GetMapping("/tiles/{layer}/tilejson.json")
    @Operation(summary = "Get TileJSON", description = "Get TileJSON metadata for a layer")
    public ResponseEntity<String> getTileJson(
            @PathVariable String layer,
            @RequestParam(defaultValue = "http://localhost:8080") String baseUrl
    ) {
        String tileJson = String.format("""
            {
              "tilejson": "3.0.0",
              "name": "%s",
              "description": "Tiles from Xenon",
              "version": "1.0.0",
              "attribution": "Xenon",
              "scheme": "xyz",
              "tiles": [
                "%s/tiles/%s/{z}/{x}/{y}.png"
              ],
              "minzoom": 0,
              "maxzoom": 18,
              "bounds": [-180, -85.0511, 180, 85.0511],
              "center": [0, 0, 2]
            }
            """, layer, baseUrl, layer);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tileJson);
    }

    /**
     * Get TileJSON for vector tiles.
     * URL pattern: /mvt/{layer}/tilejson.json
     */
    @GetMapping("/mvt/{layer}/tilejson.json")
    @Operation(summary = "Get Vector TileJSON", description = "Get TileJSON metadata for vector tiles")
    public ResponseEntity<String> getVectorTileJson(
            @PathVariable String layer,
            @RequestParam(defaultValue = "http://localhost:8080") String baseUrl
    ) {
        String tileJson = String.format("""
            {
              "tilejson": "3.0.0",
              "name": "%s",
              "description": "Vector tiles from Xenon",
              "version": "1.0.0",
              "attribution": "Xenon",
              "scheme": "xyz",
              "tiles": [
                "%s/mvt/%s/{z}/{x}/{y}.pbf"
              ],
              "minzoom": 0,
              "maxzoom": 14,
              "bounds": [-180, -85.0511, 180, 85.0511],
              "center": [0, 0, 2],
              "vector_layers": [
                {
                  "id": "%s",
                  "description": "",
                  "minzoom": 0,
                  "maxzoom": 14,
                  "fields": {}
                }
              ]
            }
            """, layer, baseUrl, layer, layer);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tileJson);
    }

    // ==================== Helper Methods ====================

    private ResponseEntity<byte[]> createTileResponse(byte[] tile, String format) {
        MediaType mediaType = switch (format.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.IMAGE_PNG;
        };
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
        headers.setContentLength(tile.length);
        
        return new ResponseEntity<>(tile, headers, HttpStatus.OK);
    }
}
