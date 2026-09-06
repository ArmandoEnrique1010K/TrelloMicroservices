package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Board;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;

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

}
