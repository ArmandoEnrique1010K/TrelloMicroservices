package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Workspace;
import com.trello.project.exception.WorkspaceNotFoundException;

public interface WorkspaceProjectService {
    List<Workspace> findAllWorkspaceByOwnerId(UUID ownerUserId);

    boolean existsWorkspaceByOwnerIdAndName(UUID ownerUserId, String name);

    boolean existsWorkspaceByOwnerIdAndNameExcludingId(
            UUID ownerUserId,
            String name,
            UUID workspaceId);

    Workspace findWorkspaceByIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId) throws WorkspaceNotFoundException;

    Workspace saveWorkspace(Workspace workspace);

    void deleteWorkspaceByIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId) throws WorkspaceNotFoundException;
}
