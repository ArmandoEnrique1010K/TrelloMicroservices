package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardAccessRepository;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.exception.BoardAccessNotFoundException;

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
    public BoardAccess findBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public boolean existsBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId) {
        return boardAccessRepository.existsByBoardIdAndUserId(boardId, userId);
    }

}
