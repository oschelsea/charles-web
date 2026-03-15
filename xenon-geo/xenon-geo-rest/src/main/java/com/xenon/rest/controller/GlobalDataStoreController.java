package com.xenon.rest.controller;

import com.xenon.core.entity.DataStore;
import com.xenon.core.entity.Workspace;
import com.xenon.core.service.DataStoreService;
import com.xenon.core.service.WorkspaceService;
import com.xenon.rest.dto.DataStoreDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Global REST API controller for viewing all data stores across workspaces.
 */
@RestController
@RequestMapping("/api/v1/datastores")
@RequiredArgsConstructor
@Tag(name = "DataStores (Global)", description = "Global data store listing operations")
public class GlobalDataStoreController {

    private final DataStoreService dataStoreService;
    private final WorkspaceService workspaceService;

    @GetMapping
    @Operation(summary = "Get all datastores", description = "Returns a list of all datastores across all workspaces")
    public ResponseEntity<DataStoreDto.DataStoreList> getAllDataStores(HttpServletRequest request) {
        List<Workspace> workspaces = workspaceService.findAll();
        Map<Long, String> wsIdToName = workspaces.stream()
                .collect(Collectors.toMap(Workspace::getId, Workspace::getName));
        
        List<DataStoreDto.Summary> allSummaries = new ArrayList<>();
        String baseUrl = getBaseUrl(request);
        
        for (Workspace ws : workspaces) {
            List<DataStore> datastores = dataStoreService.findByWorkspaceId(ws.getId());
            for (DataStore ds : datastores) {
                allSummaries.add(DataStoreDto.Summary.builder()
                        .name(ds.getName())
                        .href(baseUrl + "/api/v1/workspaces/" + ws.getName() + "/datastores/" + ds.getName())
                        .build());
            }
        }
        
        return ResponseEntity.ok(DataStoreDto.DataStoreList.builder()
                .dataStores(allSummaries)
                .build());
    }

    @GetMapping("/detail")
    @Operation(summary = "Get all datastores with details", description = "Returns detailed list of all datastores with workspace info")
    public ResponseEntity<List<DataStoreDto>> getAllDataStoresWithDetails() {
        List<Workspace> workspaces = workspaceService.findAll();
        Map<Long, String> wsIdToName = workspaces.stream()
                .collect(Collectors.toMap(Workspace::getId, Workspace::getName));
        
        List<DataStoreDto> allDataStores = new ArrayList<>();
        
        for (Workspace ws : workspaces) {
            List<DataStore> datastores = dataStoreService.findByWorkspaceId(ws.getId());
            for (DataStore ds : datastores) {
                allDataStores.add(toDto(ds, ws.getName()));
            }
        }
        
        return ResponseEntity.ok(allDataStores);
    }

    private DataStoreDto toDto(DataStore ds, String workspaceName) {
        return DataStoreDto.builder()
                .id(ds.getId())
                .name(ds.getName())
                .description(ds.getDescription())
                .type(ds.getType())
                .enabled(ds.getEnabled())
                .connectionParams(ds.getConnectionParams())
                .workspaceId(ds.getWorkspaceId())
                .workspaceName(workspaceName)
                .build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        if ((scheme.equals("http") && serverPort != 80) ||
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        url.append(contextPath);
        return url.toString();
    }
}
