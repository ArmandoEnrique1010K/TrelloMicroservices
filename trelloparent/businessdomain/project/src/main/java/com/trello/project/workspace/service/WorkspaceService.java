package com.trello.project.workspace.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.workspace.dto.request.WorkspaceRequest;
import com.trello.project.workspace.dto.response.WorkspaceResponse;
import com.trello.project.workspace.exception.WorkspaceAlreadyExistsException;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(UUID ownerUserId, WorkspaceRequest workspaceRequest)
            throws WorkspaceAlreadyExistsException;

    List<WorkspaceResponse> listAllWorkspaces(UUID ownerUserId);

    WorkspaceResponse editWorkspace(UUID ownerUserId, UUID workspaceId,
            WorkspaceRequest workspaceRequest)
            throws WorkspaceNotFoundException, WorkspaceAlreadyExistsException;

    void deleteWorkspace(UUID ownerUserId, UUID workspaceId);
}
