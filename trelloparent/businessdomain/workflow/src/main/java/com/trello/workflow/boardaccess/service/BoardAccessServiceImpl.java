package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;

@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final BoardAccessWorkflowService boardAccessWorkflowService;

    BoardAccessServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    @Override
    public void saveBoardAccess(UUID boardId, UUID userId, Role role) {

        BoardAccess savedBoardAccess = new BoardAccess();
        savedBoardAccess.setUserId(userId);
        savedBoardAccess.setBoardId(boardId);
        savedBoardAccess.setRole(role);
        savedBoardAccess.setUserActive(true);

        boardAccessWorkflowService.saveBoardAccess(savedBoardAccess);
    }

}
