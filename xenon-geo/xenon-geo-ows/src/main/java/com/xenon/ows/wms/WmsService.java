package com.xenon.ows.wms;

import com.xenon.ows.wms.request.GetCapabilitiesRequest;
import com.xenon.ows.wms.request.GetMapRequest;
import com.xenon.ows.wms.request.GetFeatureInfoRequest;
import com.xenon.ows.wms.response.WmsCapabilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * WMS (Web Map Service) implementation.
 * Supports WMS 1.1.1 and 1.3.0 specifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WmsService {

    /**
     * Handle GetCapabilities request.
     * Returns the service metadata and available layers.
     */
    public WmsCapabilities getCapabilities(GetCapabilitiesRequest request) {
        log.debug("Processing GetCapabilities request, version: {}", request.getVersion());
        
        return WmsCapabilities.builder()
                .version(request.getVersion() != null ? request.getVersion() : "1.3.0")
                .serviceTitle("Xenon WMS")
                .serviceAbstract("Web Map Service provided by Xenon")
                .onlineResource("http://localhost:8080/wms")
                .contactPerson("Xenon Admin")
                .contactOrganization("Xenon")
                .build();
    }

    /**
     * Handle GetMap request.
     * Renders and returns a map image.
     */
    public byte[] getMap(GetMapRequest request) throws IOException {
        log.debug("Processing GetMap request: layers={}, bbox={}, size={}x{}", 
                request.getLayers(), request.getBbox(), request.getWidth(), request.getHeight());

        // Create image with requested dimensions
        BufferedImage image = new BufferedImage(
                request.getWidth(), 
                request.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = image.createGraphics();
        
        try {
            // Set rendering hints for quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // If transparent, fill with transparent color, otherwise white
            if (request.isTransparent()) {
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, request.getWidth(), request.getHeight());
                g2d.setComposite(AlphaComposite.SrcOver);
            } else {
                g2d.setColor(request.getBgColor() != null ? request.getBgColor() : Color.WHITE);
                g2d.fillRect(0, 0, request.getWidth(), request.getHeight());
            }
            
            // TODO: Implement actual layer rendering using GeoTools
            // For now, render a placeholder
            renderPlaceholder(g2d, request);
            
        } finally {
            g2d.dispose();
        }
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = getImageFormatName(request.getFormat());
        ImageIO.write(image, formatName, baos);
        
        return baos.toByteArray();
    }

    /**
     * Handle GetFeatureInfo request.
     * Returns information about features at a specific location.
     */
    public String getFeatureInfo(GetFeatureInfoRequest request) {
        log.debug("Processing GetFeatureInfo request: layers={}, point=({},{})", 
                request.getQueryLayers(), request.getX(), request.getY());
        
        // TODO: Implement actual feature info query
        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        result.append("<FeatureInfoResponse>\n");
        result.append("  <Layer name=\"").append(request.getQueryLayers()).append("\">\n");
        result.append("    <Feature>\n");
        result.append("      <Attribute name=\"info\">No feature found at this location</Attribute>\n");
        result.append("    </Feature>\n");
        result.append("  </Layer>\n");
        result.append("</FeatureInfoResponse>");
        
        return result.toString();
    }

    /**
     * Render a placeholder when layer rendering is not yet implemented.
     */
    private void renderPlaceholder(Graphics2D g2d, GetMapRequest request) {
        // Draw a simple grid pattern
        g2d.setColor(new Color(200, 200, 200, 100));
        int gridSize = 50;
        for (int x = 0; x < request.getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, request.getHeight());
        }
        for (int y = 0; y < request.getHeight(); y += gridSize) {
            g2d.drawLine(0, y, request.getWidth(), y);
        }
        
        // Draw text in center
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        String text = "Xenon WMS - " + request.getLayers();
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, 
                (request.getWidth() - textWidth) / 2, 
                (request.getHeight() + textHeight) / 2);
    }

    /**
     * Convert MIME type to ImageIO format name.
     */
    private String getImageFormatName(String mimeType) {
        if (mimeType == null) {
            return "png";
        }
        return switch (mimeType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/gif" -> "gif";
            case "image/tiff" -> "tiff";
            default -> "png";
        };
    }
}
