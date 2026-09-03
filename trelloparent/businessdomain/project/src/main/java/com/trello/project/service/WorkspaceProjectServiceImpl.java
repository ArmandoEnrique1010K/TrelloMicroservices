package com.trello.project.service;

import com.trello.project.repositories.WorkspaceRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Workspace;
import com.trello.project.exception.WorkspaceNotFoundException;

@Service
public class WorkspaceProjectServiceImpl implements WorkspaceProjectService {

    private final WorkspaceRepository workspaceRepository;

    WorkspaceProjectServiceImpl(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public List<Workspace> findAllWorkspaceByOwnerId(UUID ownerUserId) {
        return workspaceRepository.findByOwnerUserId(ownerUserId);
    }

    @Override
    public boolean existsWorkspaceByOwnerIdAndName(UUID ownerUserId, String name) {
        return workspaceRepository.existsByOwnerUserIdAndName(ownerUserId, name);
    }

    @Override
    public boolean existsWorkspaceByOwnerIdAndNameExcludingId(UUID ownerUserId, String name, UUID workspaceId) {
        return workspaceRepository.existsByOwnerUserIdAndNameAndIdNot(ownerUserId, name, workspaceId);
    }

    @Override
    public Workspace saveWorkspace(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }

    // Buscar espacio de trabajo por Id y por Id del usuario propietario (al que le
    // pertenece)
    @Override
    public Workspace findWorkspaceByIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);
        return workspace;
    }

    @Override
    public void deleteWorkspaceByIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceRepository
                .findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        workspaceRepository.delete(workspace);
    }

}
