package com.xenon.core.enums;

/**
 * Enumeration of layer types.
 */
public enum LayerType {
    
    VECTOR("Vector", "Vector data layer"),
    RASTER("Raster", "Raster/Coverage layer"),
    GROUP("Group", "Layer group"),
    WMS("WMS", "Cascading WMS layer"),
    TILES3D("3DTiles", "3D Tiles layer"),
    ARCGIS_CACHE("ArcGISCache", "ArcGIS Server Cache Layer"),
    GEOPACKAGE_TILES("GeoPackageTiles", "GeoPackage Tile Layer");

    private final String name;
    private final String description;

    LayerType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
