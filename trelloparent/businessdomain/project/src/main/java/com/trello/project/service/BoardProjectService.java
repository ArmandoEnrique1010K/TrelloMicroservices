package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Board;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.member.exception.MemberNotFoundException;

public interface BoardProjectService {
    List<Board> findAllBoardsByWorkspaceIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException;

    boolean existsBoardByWorkspaceIdAndName(UUID workspaceId, String name);

    boolean existsBoardByWorkspaceIdAndNameExcludingId(
            UUID workspaceId,
            String name,
            UUID boardId);

    Board findBoardByIdAndOwnerUserId(UUID boardId, UUID ownerUserId)
            throws WorkspaceNotFoundException, BoardNotFoundException;

    Board saveBoard(Board board);

    void deleteBoardByIdAndOwnerUserId(UUID boardId, UUID ownerUserId)
            throws WorkspaceNotFoundException, BoardNotFoundException;

    // Encontrar tablero por ID y si es accesible por el ID del usuario
    Board findBoardAccessibleByUser(UUID boardId, UUID userId) throws BoardNotFoundException;

    Board findBoardByIdAndOwnerUserIdAndInactiveUserId(UUID boardId, UUID ownerUserId, UUID userId)
            throws WorkspaceNotFoundException, BoardNotFoundException, MemberNotFoundException;
}
