package com.xenon.ows.wcs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WCS endpoint controller.
 * Handles WCS 1.0.0, 1.1.0, and 2.0.0 requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "WCS", description = "OGC Web Coverage Service")
public class WcsController {

    private final WcsService wcsService;

    @GetMapping(value = {"/wcs", "/{workspace}/wcs"})
    @Operation(summary = "WCS Service Endpoint", description = "Handles WCS GetCapabilities, DescribeCoverage, and GetCoverage requests")
    public ResponseEntity<?> handleWcs(
            @PathVariable(required = false) String workspace,
            @Parameter(description = "Service type (WCS)") @RequestParam(value = "SERVICE", required = false) String service,
            @Parameter(description = "Request type") @RequestParam(value = "REQUEST") String request,
            @Parameter(description = "WCS version") @RequestParam(value = "VERSION", required = false) String version,
            @Parameter(description = "Coverage ID") @RequestParam(value = "COVERAGEID", required = false) String coverageId,
            @RequestParam(value = "COVERAGE", required = false) String coverage,
            @Parameter(description = "Output format") @RequestParam(value = "FORMAT", required = false) String format,
            @Parameter(description = "Subset (bbox, time, etc.)") @RequestParam(value = "SUBSET", required = false) String subset
    ) {
        log.debug("WCS request: request={}", request);
        
        String effectiveCoverageId = coverageId != null ? coverageId : coverage;
        
        return switch (request.toUpperCase()) {
            case "GETCAPABILITIES" -> {
                String result = wcsService.getCapabilities(version);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(result);
            }
            case "DESCRIBECOVERAGE" -> {
                String result = wcsService.describeCoverage(effectiveCoverageId, version);
                yield ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_XML)
                        .body(result);
            }
            case "GETCOVERAGE" -> {
                byte[] data = wcsService.getCoverage(effectiveCoverageId, format, subset);
                
                if (data == null || data.length == 0) {
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(createException("CoverageNotFound", 
                                    "Coverage not found: " + effectiveCoverageId));
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(
                        format != null ? format : "image/tiff"));
                headers.setContentLength(data.length);
                
                yield new ResponseEntity<>(data, headers, HttpStatus.OK);
            }
            default -> ResponseEntity.badRequest()
                    .body(createException("InvalidRequest", "Unknown request: " + request));
        };
    }

    private String createException(String code, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/2.0\" version=\"2.0.0\">\n" +
                "  <ows:Exception exceptionCode=\"" + code + "\">\n" +
                "    <ows:ExceptionText>" + message + "</ows:ExceptionText>\n" +
                "  </ows:Exception>\n" +
                "</ows:ExceptionReport>";
    }
}
