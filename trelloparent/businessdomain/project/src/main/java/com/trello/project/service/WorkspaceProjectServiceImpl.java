package com.trello.project.service;

import com.trello.project.repositories.BoardRepository;
import com.trello.project.repositories.WorkspaceRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Workspace;
import com.trello.project.exception.WorkspaceNotFoundException;

@Service
public class WorkspaceProjectServiceImpl implements WorkspaceProjectService {

    private final WorkspaceRepository workspaceRepository;
    private final BoardRepository boardRepository;

    public WorkspaceProjectServiceImpl(WorkspaceRepository workspaceRepository,
            BoardRepository boardRepository) {
        this.workspaceRepository = workspaceRepository;
        this.boardRepository = boardRepository;
    }

    @Override
    public List<Workspace> findAllWorkspaceByOwnerId(UUID ownerUserId) {
        return workspaceRepository.findByOwnerUserIdAndStatusTrue(ownerUserId);
    }

    @Override
    public boolean existsWorkspaceByOwnerIdAndName(UUID ownerUserId, String name) {
        return workspaceRepository.existsByOwnerUserIdAndNameAndStatusTrue(ownerUserId, name);
    }

    @Override
    public boolean existsWorkspaceByOwnerIdAndNameExcludingId(UUID ownerUserId, String name, UUID workspaceId) {
        return workspaceRepository.existsByOwnerUserIdAndNameAndIdNotAndStatusTrue(ownerUserId, name, workspaceId);
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
    public Workspace findWorkspaceByIdAndOwnerUserIdAndStatusTrue(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceRepository.findByIdAndOwnerUserIdAndStatusTrue(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);
        return workspace;
    }

    @Override
    public void deleteWorkspaceByIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceRepository
                .findByIdAndOwnerUserIdAndStatusTrue(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        workspaceRepository.delete(workspace);
    }

    @Override
    public void changeStatusWorkspaceById(UUID workspaceId, UUID ownerUserId, boolean status)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceRepository
                .findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        workspace.setStatus(status);

        // Cambiar el estado de cada tablero asociado al workspace
        if (workspace.getBoards() != null) {
            workspace.getBoards().forEach(board -> {
                if (board != null) {
                    board.setStatus(status);
                    boardRepository.save(board);
                }
            });
        }

        workspaceRepository.save(workspace);
    }

}
