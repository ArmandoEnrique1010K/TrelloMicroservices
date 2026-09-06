package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Board;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;

public interface BoardProjectService {
    List<Board> findAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId) throws WorkspaceNotFoundException;

    boolean existsBoardByWorkspaceIdAndName(UUID workspaceId, UUID ownerUserId, String name)
            throws WorkspaceNotFoundException;

    boolean existsBoardByWorkspaceIdAndNameExcludingId(
            UUID workspaceId,
            String name,
            UUID ownerUserId,
            UUID boardId) throws WorkspaceNotFoundException;

    Board findBoardByIdAndWorkspaceId(UUID boardId, UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException, BoardNotFoundException;

    Board saveBoard(Board board, UUID ownerUserId) throws WorkspaceNotFoundException;

    void deleteBoardByIdAndWorkspaceId(UUID boardId, UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException, BoardNotFoundException;
}
