package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardAccessRepository;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.exception.BusinessRuleException;

@Service
public class BoardAccessWorkflowServiceImpl implements BoardAccessWorkflowService {

    private final BoardAccessRepository boardAccessRepository;

    BoardAccessWorkflowServiceImpl(BoardAccessRepository boardAccessRepository) {
        this.boardAccessRepository = boardAccessRepository;
    }

    @Override
    public void saveBoardAccess(BoardAccess boardAccess) {
        boardAccessRepository.save(boardAccess);
    }

    @Override
    public BoardAccess findBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId) {
        // TODO: Mejorar el mensaje de error
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(BusinessRuleException::new);
        return boardAccess;
    }

}
