package com.xenon.ows.wfs;

import com.xenon.ows.wfs.request.DescribeFeatureTypeRequest;
import com.xenon.ows.wfs.request.GetFeatureRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WFS (Web Feature Service) implementation.
 * Supports WFS 1.0.0, 1.1.0 and 2.0.0 specifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfsService {

    /**
     * Handle GetCapabilities request.
     */
    public String getCapabilities(String version) {
        log.debug("Processing WFS GetCapabilities, version: {}", version);
        
        String ver = version != null ? version : "2.0.0";
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<wfs:WFS_Capabilities version=\"").append(ver).append("\" ");
        xml.append("xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" ");
        xml.append("xmlns:ows=\"http://www.opengis.net/ows/1.1\" ");
        xml.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        
        // Service Identification
        xml.append("  <ows:ServiceIdentification>\n");
        xml.append("    <ows:Title>Xenon WFS</ows:Title>\n");
        xml.append("    <ows:Abstract>Web Feature Service provided by Xenon</ows:Abstract>\n");
        xml.append("    <ows:ServiceType>WFS</ows:ServiceType>\n");
        xml.append("    <ows:ServiceTypeVersion>").append(ver).append("</ows:ServiceTypeVersion>\n");
        xml.append("  </ows:ServiceIdentification>\n");
        
        // Operations Metadata
        xml.append("  <ows:OperationsMetadata>\n");
        appendOperation(xml, "GetCapabilities", "http://localhost:8080/wfs");
        appendOperation(xml, "DescribeFeatureType", "http://localhost:8080/wfs");
        appendOperation(xml, "GetFeature", "http://localhost:8080/wfs");
        xml.append("  </ows:OperationsMetadata>\n");
        
        // Feature Type List
        xml.append("  <wfs:FeatureTypeList>\n");
        xml.append("  </wfs:FeatureTypeList>\n");
        
        xml.append("</wfs:WFS_Capabilities>");
        
        return xml.toString();
    }

    /**
     * Handle DescribeFeatureType request.
     */
    public String describeFeatureType(DescribeFeatureTypeRequest request) {
        log.debug("Processing DescribeFeatureType: typeName={}", request.getTypeName());
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
        xml.append("xmlns:gml=\"http://www.opengis.net/gml/3.2\" ");
        xml.append("xmlns:xenon=\"http://xenon.com/wfs\" ");
        xml.append("targetNamespace=\"http://xenon.com/wfs\" ");
        xml.append("elementFormDefault=\"qualified\">\n");
        
        xml.append("  <xsd:import namespace=\"http://www.opengis.net/gml/3.2\" ");
        xml.append("schemaLocation=\"http://schemas.opengis.net/gml/3.2.1/gml.xsd\"/>\n");
        
        // TODO: Generate actual schema from feature type
        xml.append("  <xsd:complexType name=\"FeatureType\">\n");
        xml.append("    <xsd:complexContent>\n");
        xml.append("      <xsd:extension base=\"gml:AbstractFeatureType\">\n");
        xml.append("        <xsd:sequence>\n");
        xml.append("          <xsd:element name=\"geometry\" type=\"gml:GeometryPropertyType\" minOccurs=\"0\"/>\n");
        xml.append("          <xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\"/>\n");
        xml.append("        </xsd:sequence>\n");
        xml.append("      </xsd:extension>\n");
        xml.append("    </xsd:complexContent>\n");
        xml.append("  </xsd:complexType>\n");
        
        xml.append("</xsd:schema>");
        
        return xml.toString();
    }

    /**
     * Handle GetFeature request.
     */
    public String getFeature(GetFeatureRequest request) {
        log.debug("Processing GetFeature: typeName={}, maxFeatures={}", 
                request.getTypeName(), request.getMaxFeatures());
        
        String outputFormat = request.getOutputFormat();
        
        if (outputFormat != null && outputFormat.toLowerCase().contains("json")) {
            return getFeatureAsGeoJson(request);
        } else {
            return getFeatureAsGml(request);
        }
    }

    private String getFeatureAsGml(GetFeatureRequest request) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<wfs:FeatureCollection ");
        xml.append("xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" ");
        xml.append("xmlns:gml=\"http://www.opengis.net/gml/3.2\" ");
        xml.append("xmlns:xenon=\"http://xenon.com/wfs\" ");
        xml.append("numberMatched=\"0\" numberReturned=\"0\">\n");
        
        // TODO: Query actual features from data store
        
        xml.append("</wfs:FeatureCollection>");
        
        return xml.toString();
    }

    private String getFeatureAsGeoJson(GetFeatureRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"type\": \"FeatureCollection\",\n");
        json.append("  \"features\": [],\n");
        json.append("  \"totalFeatures\": 0,\n");
        json.append("  \"numberMatched\": 0,\n");
        json.append("  \"numberReturned\": 0\n");
        json.append("}");
        
        return json.toString();
    }

    private void appendOperation(StringBuilder xml, String name, String url) {
        xml.append("    <ows:Operation name=\"").append(name).append("\">\n");
        xml.append("      <ows:DCP>\n");
        xml.append("        <ows:HTTP>\n");
        xml.append("          <ows:Get xlink:href=\"").append(url).append("?\"/>\n");
        xml.append("          <ows:Post xlink:href=\"").append(url).append("\"/>\n");
        xml.append("        </ows:HTTP>\n");
        xml.append("      </ows:DCP>\n");
        xml.append("    </ows:Operation>\n");
    }
}
