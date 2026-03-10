package com.xenon.rest.controller;

import com.xenon.core.entity.Style;
import com.xenon.core.enums.StyleFormat;
import com.xenon.core.service.StyleService;
import com.xenon.rest.dto.StyleDto;
import com.xenon.rest.mapper.StyleRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/styles")
@RequiredArgsConstructor
@Tag(name = "Styles", description = "Style management operations")
public class StyleController {

    private final StyleService styleService;
    private final StyleRestMapper styleMapper;

    @GetMapping
    @Operation(summary = "Get all global styles")
    public ResponseEntity<StyleDto.StyleList> getAllStyles(HttpServletRequest request) {
        List<Style> styles = styleService.findAllGlobal();
        String baseUrl = getBaseUrl(request);
        
        List<StyleDto.Summary> summaries = styles.stream()
                .map(s -> styleMapper.toSummary(s, baseUrl))
                .toList();
        
        return ResponseEntity.ok(StyleDto.StyleList.builder()
                .styles(summaries)
                .build());
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get a single style metadata")
    public ResponseEntity<StyleDto.StyleWrapper> getStyle(@PathVariable String name) {
        Style style = styleService.findByName(name);
        return ResponseEntity.ok(StyleDto.StyleWrapper.builder()
                .style(styleMapper.toDto(style))
                .build());
    }

    @GetMapping(value = "/{name}/content", produces = {"application/vnd.ogc.sld+xml", "text/css", "application/json", "application/yaml", "text/plain"})
    @Operation(summary = "Get style content")
    public ResponseEntity<String> getStyleContent(@PathVariable String name) {
        Style style = styleService.findByName(name);
        return ResponseEntity.ok(style.getContent() == null ? "" : style.getContent());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new style")
    public ResponseEntity<StyleDto.StyleWrapper> createStyle(@Valid @RequestBody StyleDto.StyleWrapper request) {
        Style style = styleMapper.toEntity(request.getStyle());
        Style created = styleService.create(style);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StyleDto.StyleWrapper.builder()
                        .style(styleMapper.toDto(created))
                        .build());
    }

    @PutMapping("/{name}")
    @Operation(summary = "Update style metadata")
    public ResponseEntity<StyleDto.StyleWrapper> updateStyle(
            @PathVariable String name,
            @Valid @RequestBody StyleDto.StyleWrapper request) {
        Style updates = styleMapper.toEntity(request.getStyle());
        Style updated = styleService.update(name, updates);
        return ResponseEntity.ok(StyleDto.StyleWrapper.builder()
                .style(styleMapper.toDto(updated))
                .build());
    }

    @PutMapping(value = "/{name}/content", consumes = {"application/vnd.ogc.sld+xml", "text/css", "application/json", "application/yaml", "text/plain"})
    @Operation(summary = "Update style content")
    public ResponseEntity<Void> updateStyleContent(
            @PathVariable String name,
            @RequestBody String content) {
        styleService.updateContent(name, content);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a style")
    public ResponseEntity<Void> deleteStyle(@PathVariable String name) {
        styleService.delete(name);
        return ResponseEntity.noContent().build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath);
        return url.toString();
    }
}
