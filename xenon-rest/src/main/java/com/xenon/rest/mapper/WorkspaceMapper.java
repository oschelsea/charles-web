package com.xenon.rest.mapper;

import com.xenon.core.entity.Workspace;
import com.xenon.rest.dto.WorkspaceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Workspace entity and DTO conversions.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkspaceMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Mapping(target = "updatedAt", source = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    WorkspaceDto toDto(Workspace workspace);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Workspace toEntity(WorkspaceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(WorkspaceDto dto, @MappingTarget Workspace workspace);

    default WorkspaceDto.Summary toSummary(Workspace workspace, String baseUrl) {
        return WorkspaceDto.Summary.builder()
                .name(workspace.getName())
                .href(baseUrl + "/api/v1/workspaces/" + workspace.getName())
                .build();
    }

    List<WorkspaceDto> toDtoList(List<Workspace> workspaces);
}
