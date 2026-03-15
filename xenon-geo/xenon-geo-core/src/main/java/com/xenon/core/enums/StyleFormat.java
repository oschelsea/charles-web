package com.xenon.core.enums;

/**
 * Enumeration of supported style formats.
 */
public enum StyleFormat {
    
    SLD("SLD", "application/vnd.ogc.sld+xml", ".sld"),
    SLD_1_1("SLD 1.1", "application/vnd.ogc.se+xml", ".sld"),
    CSS("CSS", "application/vnd.geoserver.geocss+css", ".css"),
    MBSTYLE("MBStyle", "application/vnd.geoserver.mbstyle+json", ".json"),
    YSLD("YSLD", "application/vnd.geoserver.ysld+yaml", ".ysld");

    private final String name;
    private final String mimeType;
    private final String extension;

    StyleFormat(String name, String mimeType, String extension) {
        this.name = name;
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
