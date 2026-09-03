package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Workspace;

public interface WorkspaceProjectService {
    List<Workspace> findAllWorkspaceByOwnerId(UUID ownerId);
}
