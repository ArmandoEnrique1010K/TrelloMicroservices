package com.trello.project.service;

import com.trello.project.repositories.WorkspaceRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Workspace;

@Service
public class WorkspaceProjectServiceImpl implements WorkspaceProjectService {

    private final WorkspaceRepository workspaceRepository;

    WorkspaceProjectServiceImpl(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public List<Workspace> findAllWorkspaceByOwnerId(UUID ownerId) {
        return workspaceRepository.findByOwnerUserId(ownerId);
    }

}
