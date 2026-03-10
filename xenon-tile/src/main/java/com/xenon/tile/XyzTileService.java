package com.xenon.tile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * XYZ/TMS Tile Service implementation.
 * Provides raster tiles in standard web mercator tiling scheme.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XyzTileService {

    private static final int TILE_SIZE = 256;

    /**
     * Get a raster tile for the specified layer.
     * 
     * @param layer Layer name
     * @param z Zoom level
     * @param x Column (X coordinate)
     * @param y Row (Y coordinate, TMS or XYZ scheme)
     * @param format Output format (png, jpg, etc.)
     * @param tms Whether to use TMS (Y-axis flipped) or XYZ scheme
     * @return Tile image as byte array
     */
    public byte[] getTile(String layer, int z, int x, int y, String format, boolean tms) throws IOException {
        log.debug("XYZ tile request: layer={}, z={}, x={}, y={}, tms={}", layer, z, x, y, tms);
        
        // Convert TMS Y to XYZ Y if needed
        int tileY = tms ? flipY(z, y) : y;
        
        // TODO: Replace with actual layer rendering using GeoTools
        return renderPlaceholderTile(layer, z, x, tileY, format);
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
     * Flip Y coordinate between TMS and XYZ schemes.
     */
    private int flipY(int z, int y) {
        return (1 << z) - 1 - y;
    }

    /**
     * Render a placeholder tile for testing.
     */
    private byte[] renderPlaceholderTile(String layer, int z, int x, int y, String format) throws IOException {
        BufferedImage image = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Transparent background
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
            g2d.setComposite(AlphaComposite.SrcOver);
            
            // Draw grid
            g2d.setColor(new Color(100, 100, 100, 80));
            g2d.drawRect(0, 0, TILE_SIZE - 1, TILE_SIZE - 1);
            
            // Draw cross lines
            g2d.setColor(new Color(100, 100, 100, 40));
            g2d.drawLine(0, TILE_SIZE / 2, TILE_SIZE, TILE_SIZE / 2);
            g2d.drawLine(TILE_SIZE / 2, 0, TILE_SIZE / 2, TILE_SIZE);
            
            // Draw tile info
            g2d.setColor(new Color(60, 60, 60, 180));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            
            String zInfo = String.format("Z: %d", z);
            String xyInfo = String.format("X: %d  Y: %d", x, y);
            String layerInfo = layer != null ? layer : "no layer";
            
            FontMetrics fm = g2d.getFontMetrics();
            int textY = TILE_SIZE / 2 - 10;
            
            g2d.drawString(zInfo, (TILE_SIZE - fm.stringWidth(zInfo)) / 2, textY);
            g2d.drawString(xyInfo, (TILE_SIZE - fm.stringWidth(xyInfo)) / 2, textY + 15);
            g2d.drawString(layerInfo, (TILE_SIZE - fm.stringWidth(layerInfo)) / 2, textY + 30);
            
        } finally {
            g2d.dispose();
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = "jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format) ? "jpg" : "png";
        ImageIO.write(image, formatName, baos);
        
        return baos.toByteArray();
    }
}
