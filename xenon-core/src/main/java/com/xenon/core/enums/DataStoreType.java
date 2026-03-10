package com.xenon.core.enums;

/**
 * Enumeration of supported data store types.
 */
public enum DataStoreType {
    
    POSTGIS("PostGIS", "PostgreSQL/PostGIS Database", true),
    SHAPEFILE("Shapefile", "ESRI Shapefile", true),
    GEOPACKAGE("GeoPackage", "OGC GeoPackage", true),
    GEOJSON("GeoJSON", "GeoJSON File", true),
    GEOTIFF("GeoTIFF", "GeoTIFF Raster", false),
    IMAGE_MOSAIC("ImageMosaic", "Image Mosaic", false),
    WMS("WMS", "Web Map Service (Cascading)", false),
    WFS("WFS", "Web Feature Service (Cascading)", true),
    TILES3D_CACHE("3DTiles", "3D Tiles Cache Data", false),
    ARCGIS_CACHE("ArcGISCache", "ArcGIS Server Cache", false);

    private final String name;
    private final String description;
    private final boolean vector;

    DataStoreType(String name, String description, boolean vector) {
        this.name = name;
        this.description = description;
        this.vector = vector;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVector() {
        return vector;
    }

    public boolean isRaster() {
        return !vector;
    }
}
