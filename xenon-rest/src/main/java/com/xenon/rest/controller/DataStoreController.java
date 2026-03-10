package com.xenon.rest.controller;

import com.xenon.core.entity.DataStore;
import com.xenon.core.entity.Workspace;
import com.xenon.core.service.DataStoreService;
import com.xenon.core.service.WorkspaceService;
import com.xenon.rest.dto.DataStoreDto;
import com.xenon.rest.mapper.DataStoreRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for managing data stores within a workspace.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceName}/datastores")
@RequiredArgsConstructor
@Tag(name = "DataStores", description = "Data store management operations")
public class DataStoreController {

    private final DataStoreService dataStoreService;
    private final WorkspaceService workspaceService;
    private final DataStoreRestMapper dataStoreMapper;

    @GetMapping
    @Operation(summary = "Get all datastores", description = "Returns a list of all datastores in a workspace")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved datastores"),
        @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    public ResponseEntity<DataStoreDto.DataStoreList> getAllDataStores(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            HttpServletRequest request) {
        Workspace workspace = workspaceService.findByName(workspaceName);
        List<DataStore> dataStores = dataStoreService.findByWorkspaceId(workspace.getId());
        String baseUrl = getBaseUrl(request);
        
        List<DataStoreDto.Summary> summaries = dataStores.stream()
                .map(ds -> dataStoreMapper.toSummary(ds, baseUrl, workspaceName))
                .toList();
        
        return ResponseEntity.ok(DataStoreDto.DataStoreList.builder()
                .dataStores(summaries)
                .build());
    }

    @GetMapping("/{datastoreName}")
    @Operation(summary = "Get datastore by name", description = "Returns a single datastore")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved datastore"),
        @ApiResponse(responseCode = "404", description = "DataStore not found")
    })
    public ResponseEntity<DataStoreDto.DataStoreWrapper> getDataStore(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName) {
        Workspace workspace = workspaceService.findByName(workspaceName);
        DataStore dataStore = dataStoreService.findByWorkspaceAndName(workspace.getId(), datastoreName);
        DataStoreDto dto = dataStoreMapper.toDto(dataStore);
        
        return ResponseEntity.ok(DataStoreDto.DataStoreWrapper.builder()
                .dataStore(dto)
                .build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create datastore", description = "Creates a new datastore in the workspace")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "DataStore created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Workspace not found"),
        @ApiResponse(responseCode = "409", description = "DataStore already exists")
    })
    public ResponseEntity<DataStoreDto.DataStoreWrapper> createDataStore(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Valid @RequestBody DataStoreDto.DataStoreWrapper request) {
        // Validate datastore name
        com.xenon.core.util.NameValidator.validateName(request.getDataStore().getName(), "datastore");
        
        Workspace workspace = workspaceService.findByName(workspaceName);
        
        DataStore dataStore = dataStoreMapper.toEntity(request.getDataStore());
        dataStore.setWorkspaceId(workspace.getId());
        
        DataStore created = dataStoreService.create(dataStore);
        DataStoreDto dto = dataStoreMapper.toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DataStoreDto.DataStoreWrapper.builder()
                        .dataStore(dto)
                        .build());
    }

    @PutMapping("/{datastoreName}")
    @Operation(summary = "Update datastore", description = "Updates an existing datastore")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "DataStore updated successfully"),
        @ApiResponse(responseCode = "404", description = "DataStore not found")
    })
    public ResponseEntity<DataStoreDto.DataStoreWrapper> updateDataStore(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName,
            @Valid @RequestBody DataStoreDto.DataStoreWrapper request) {
        Workspace workspace = workspaceService.findByName(workspaceName);
        
        DataStore updates = dataStoreMapper.toEntity(request.getDataStore());
        DataStore updated = dataStoreService.update(workspace.getId(), datastoreName, updates);
        DataStoreDto dto = dataStoreMapper.toDto(updated);
        
        return ResponseEntity.ok(DataStoreDto.DataStoreWrapper.builder()
                .dataStore(dto)
                .build());
    }

    @DeleteMapping("/{datastoreName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete datastore", description = "Deletes a datastore")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "DataStore deleted successfully"),
        @ApiResponse(responseCode = "404", description = "DataStore not found")
    })
    public ResponseEntity<Void> deleteDataStore(
            @Parameter(description = "Workspace name") @PathVariable String workspaceName,
            @Parameter(description = "DataStore name") @PathVariable String datastoreName) {
        Workspace workspace = workspaceService.findByName(workspaceName);
        dataStoreService.delete(workspace.getId(), datastoreName);
        return ResponseEntity.noContent().build();
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
