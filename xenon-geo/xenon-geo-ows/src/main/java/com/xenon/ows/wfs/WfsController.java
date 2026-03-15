package com.xenon.ows.wfs;

import com.xenon.ows.wfs.request.DescribeFeatureTypeRequest;
import com.xenon.ows.wfs.request.GetFeatureRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WFS endpoint controller.
 * Handles WFS 1.0.0, 1.1.0, and 2.0.0 requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "WFS", description = "OGC Web Feature Service")
public class WfsController {

    private final WfsService wfsService;

    @GetMapping(value = {"/wfs", "/{workspace}/wfs"})
    @Operation(summary = "WFS Service Endpoint", description = "Handles WFS GetCapabilities, DescribeFeatureType, and GetFeature requests")
    public ResponseEntity<String> handleWfs(
            @PathVariable(required = false) String workspace,
            @Parameter(description = "Service type (WFS)") @RequestParam(value = "SERVICE", required = false) String service,
            @Parameter(description = "Request type") @RequestParam(value = "REQUEST") String request,
            @Parameter(description = "WFS version") @RequestParam(value = "VERSION", required = false) String version,
            @Parameter(description = "Type name (WFS 1.x)") @RequestParam(value = "TYPENAME", required = false) String typeName,
            @Parameter(description = "Type names (WFS 2.0)") @RequestParam(value = "TYPENAMES", required = false) String typeNames,
            @Parameter(description = "Output format") @RequestParam(value = "OUTPUTFORMAT", required = false) String outputFormat,
            @Parameter(description = "Max features (WFS 1.x)") @RequestParam(value = "MAXFEATURES", required = false) Integer maxFeatures,
            @Parameter(description = "Count (WFS 2.0)") @RequestParam(value = "COUNT", required = false) Integer count,
            @Parameter(description = "Start index") @RequestParam(value = "STARTINDEX", required = false) Integer startIndex,
            @Parameter(description = "SRS name") @RequestParam(value = "SRSNAME", required = false) String srsName,
            @Parameter(description = "Bounding box") @RequestParam(value = "BBOX", required = false) String bbox,
            @Parameter(description = "CQL filter") @RequestParam(value = "CQL_FILTER", required = false) String cqlFilter,
            @Parameter(description = "Property names") @RequestParam(value = "PROPERTYNAME", required = false) String propertyName,
            @Parameter(description = "Sort by") @RequestParam(value = "SORTBY", required = false) String sortBy
    ) {
        log.debug("WFS request: workspace={}, request={}", workspace, request);
        
        return switch (request.toUpperCase()) {
            case "GETCAPABILITIES" -> {
                String result = wfsService.getCapabilities(version);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(result);
            }
            case "DESCRIBEFEATURETYPE" -> {
                DescribeFeatureTypeRequest req = DescribeFeatureTypeRequest.builder()
                        .version(version)
                        .typeName(typeName != null ? typeName : typeNames)
                        .outputFormat(outputFormat)
                        .build();
                String result = wfsService.describeFeatureType(req);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(result);
            }
            case "GETFEATURE" -> {
                GetFeatureRequest req = GetFeatureRequest.builder()
                        .version(version)
                        .typeName(typeName)
                        .typeNames(typeNames)
                        .outputFormat(outputFormat)
                        .maxFeatures(maxFeatures)
                        .count(count)
                        .startIndex(startIndex)
                        .srsName(srsName)
                        .bbox(bbox)
                        .cqlFilter(cqlFilter)
                        .propertyName(propertyName)
                        .sortBy(sortBy)
                        .build();
                String result = wfsService.getFeature(req);
                
                MediaType mediaType = outputFormat != null && outputFormat.toLowerCase().contains("json")
                        ? MediaType.APPLICATION_JSON
                        : MediaType.APPLICATION_XML;
                
                yield ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(result);
            }
            default -> ResponseEntity.badRequest()
                    .body(createServiceException("InvalidRequest", "Unknown request: " + request));
        };
    }

    private String createServiceException(String code, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/1.1\" version=\"2.0.0\">\n" +
                "  <ows:Exception exceptionCode=\"" + code + "\">\n" +
                "    <ows:ExceptionText>" + message + "</ows:ExceptionText>\n" +
                "  </ows:Exception>\n" +
                "</ows:ExceptionReport>";
    }
}
