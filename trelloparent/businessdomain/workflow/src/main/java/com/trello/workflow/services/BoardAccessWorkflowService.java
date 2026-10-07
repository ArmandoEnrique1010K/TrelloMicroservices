package com.trello.workflow.services;

import java.util.UUID;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.exception.BoardAccessNotFoundException;

public interface BoardAccessWorkflowService {
    void saveBoardAccess(BoardAccess boardAccess);

    BoardAccess findBoardAccessByBoardIdAndUserIdAndUserActive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    boolean existsBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId);

    BoardAccess findBoardAccessByBoardIdAndUserIdAndUserInactive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;
}
