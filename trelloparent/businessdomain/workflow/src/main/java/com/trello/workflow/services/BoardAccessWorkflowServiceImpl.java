package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardAccessRepository;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
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
    public BoardAccess findBoardAccessByBoardIdAndUserIdAndUserActive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndUserActiveTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public boolean existsBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId) {
        return boardAccessRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    @Override
    public BoardAccess findBoardAccessByBoardIdAndUserIdAndUserInactive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndUserActiveFalse(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public void deleteBoardAccess(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        boardAccessRepository.delete(boardAccess);
    }

    @Override
    public void revertActivateBoardAccess(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);

        boardAccess.setUserActive(false);
        boardAccess.setRole(previousRole);

        boardAccessRepository.save(boardAccess);
    }

}
