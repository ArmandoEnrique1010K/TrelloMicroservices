package com.trello.workflow.services;

import java.util.UUID;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BoardAccessNotFoundException;

public interface BoardAccessWorkflowService {
    void saveBoardAccess(BoardAccess boardAccess);

    BoardAccess findBoardAccessByBoardIdAndUserIdAndUserActiveAndStatusTrue(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    boolean existsBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId);

    BoardAccess findBoardAccessByBoardIdAndUserIdAndUserInactiveAndStatusTrue(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    void deleteBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException;

    void inactivateBoardAccessAndChangeRoleByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException;

    void changeBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException;

    void activateBoardAccessAndChangeRoleByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException;
}
