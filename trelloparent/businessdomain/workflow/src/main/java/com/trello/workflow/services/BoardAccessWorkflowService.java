package com.trello.workflow.services;

import java.util.UUID;

import com.trello.workflow.entities.BoardAccess;

public interface BoardAccessWorkflowService {
    void saveBoardAccess(BoardAccess boardAccess);

    BoardAccess findBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId);
}
