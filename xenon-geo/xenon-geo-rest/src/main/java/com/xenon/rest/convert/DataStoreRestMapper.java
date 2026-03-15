package com.xenon.rest.convert;

import com.xenon.core.entity.DataStore;
import com.xenon.rest.dto.DataStoreDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for DataStore entity and DTO conversions.
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataStoreRestMapper {

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Mapping(target = "updatedAt", source = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    DataStoreDto toDto(DataStore dataStore);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    DataStore toEntity(DataStoreDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "workspaceId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(DataStoreDto dto, @MappingTarget DataStore dataStore);

    default DataStoreDto.Summary toSummary(DataStore dataStore, String baseUrl, String workspaceName) {
        return DataStoreDto.Summary.builder()
                .name(dataStore.getName())
                .href(baseUrl + "/api/v1/workspaces/" + workspaceName + "/datastores/" + dataStore.getName())
                .build();
    }

    List<DataStoreDto> toDtoList(List<DataStore> dataStores);
}
