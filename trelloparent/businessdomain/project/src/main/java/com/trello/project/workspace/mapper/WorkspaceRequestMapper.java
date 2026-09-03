package com.trello.project.workspace.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.project.entities.Workspace;
import com.trello.project.workspace.dto.request.WorkspaceRequest;

@Mapper(componentModel = "spring")
public interface WorkspaceRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "ownerUserId", ignore = true),
            @Mapping(target = "boards", ignore = true),
            @Mapping(target = "createdAt", ignore = true)
    })
    Workspace workspaceRequestToWorkspace(WorkspaceRequest source);
}
