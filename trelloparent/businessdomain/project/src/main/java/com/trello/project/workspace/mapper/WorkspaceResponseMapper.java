package com.trello.project.workspace.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.project.entities.Workspace;
import com.trello.project.workspace.dto.response.WorkspaceResponse;

@Mapper(componentModel = "spring")
public interface WorkspaceResponseMapper {

    WorkspaceResponse workspaceToWorkspaceResponse(Workspace source);

    List<WorkspaceResponse> workspaceListToWorkspaceResponseList(List<Workspace> source);
}
