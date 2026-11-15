package com.trello.project.service;

import com.trello.project.repositories.BoardRepository;
import com.trello.project.repositories.WorkspaceRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Workspace;
import com.trello.project.enums.BoardStatus;
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
        return workspaceRepository.findByOwnerUserIdAndStatusTrueOrderByCreatedAtDesc(ownerUserId);
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
                .findByIdAndOwnerUserIdAndStatusFalse(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        workspaceRepository.delete(workspace);
    }

    // TODO: URGENTE, CAMBIAR EL PARAMETRO STATUS
    @Override
    public void changeStatusWorkspaceById(UUID workspaceId, UUID ownerUserId, boolean workspaceStatus,
            BoardStatus expectedBoardStatus,
            BoardStatus convertBoardStatus)
            throws WorkspaceNotFoundException {
        Workspace workspace = workspaceRepository
                .findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        workspace.setStatus(workspaceStatus);

        // Pueden haber tableros con estado DELETED, es por ello que se tiene 3 estados
        // en board:
        // ACTIVE, DELETING y DELETED

        // Filtrar tableros que tengan estado ACTIVE
        if (workspace.getBoards() != null) {
            workspace.getBoards().stream()
                    .filter(board -> board != null && board.getStatus() == expectedBoardStatus)
                    .forEach(board -> {
                        board.setStatus(convertBoardStatus);
                        boardRepository.save(board);
                    });
        }

        workspaceRepository.save(workspace);
    }

    @Override
    public Workspace findWorkspaceByIdWithLock(UUID workspaceId) {
        return workspaceRepository.findByIdWithLock(workspaceId)
                .orElseThrow(WorkspaceNotFoundException::new);
    }

}
