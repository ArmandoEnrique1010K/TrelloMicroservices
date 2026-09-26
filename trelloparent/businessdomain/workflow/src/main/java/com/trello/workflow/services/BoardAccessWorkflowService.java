package com.trello.workflow.services;

import java.util.UUID;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.exception.BoardAccessNotFoundException;

public interface BoardAccessWorkflowService {
    void saveBoardAccess(BoardAccess boardAccess);

    BoardAccess findBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    BoardAccess findBoardAccessByBoardIdAndUserIdAndRoleOwner(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    boolean existsBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId);

    void deleteAllBoardAccessByBoardId(UUID boardId);
}
