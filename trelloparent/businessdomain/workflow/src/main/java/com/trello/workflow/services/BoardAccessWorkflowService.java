package com.trello.workflow.services;

import java.util.UUID;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BoardAccessNotFoundException;

public interface BoardAccessWorkflowService {
    void saveBoardAccess(BoardAccess boardAccess);

    BoardAccess findBoardAccessByBoardIdAndUserIdAndUserActive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    boolean existsBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId);

    BoardAccess findBoardAccessByBoardIdAndUserIdAndUserInactive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    void deleteBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    void inactivateBoardAccessAndChangeRoleByBoardIdAndUserId(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException;

    void changeBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException;

    void activateBoardAccessAndChangeRoleByBoardIdAndUserId(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException;

    // void disableAllBoardAccessStatusByBoardId(UUID boardId);

    // void enableAllBoardAccessStatusByBoardId(UUID boardId);
}
