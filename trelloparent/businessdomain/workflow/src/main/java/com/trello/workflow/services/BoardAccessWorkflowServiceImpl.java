package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardAccessRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BoardAccessNotFoundException;

@Slf4j
@Service
public class BoardAccessWorkflowServiceImpl implements BoardAccessWorkflowService {

    private final BoardAccessRepository boardAccessRepository;

    public BoardAccessWorkflowServiceImpl(BoardAccessRepository boardAccessRepository) {
        this.boardAccessRepository = boardAccessRepository;
    }

    @Override
    public void saveBoardAccess(BoardAccess boardAccess) {
        boardAccessRepository.save(boardAccess);
    }

    @Override
    public BoardAccess findBoardAccessByBoardIdAndUserIdAndUserActiveAndStatusTrue(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository
                .findByBoardIdAndUserIdAndUserActiveTrueAndStatusTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public boolean existsBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId) {
        return boardAccessRepository.existsByBoardIdAndUserIdAndStatusTrue(boardId, userId);
    }

    @Override
    public BoardAccess findBoardAccessByBoardIdAndUserIdAndUserInactiveAndStatusTrue(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository
                .findByBoardIdAndUserIdAndUserActiveFalseAndStatusTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public void deleteBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        boardAccessRepository.delete(boardAccess);
    }

    @Override
    public void inactivateBoardAccessAndChangeRoleByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId,
            Role previousRole)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(
                boardId, userId).orElseThrow(BoardAccessNotFoundException::new);

        boardAccess.setRole(previousRole);
        boardAccess.setUserActive(false);
        boardAccessRepository.save(boardAccess);
    }

    @Override
    public void changeBoardAccessByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(
                boardId, userId).orElseThrow(BoardAccessNotFoundException::new);

        boardAccess.setRole(previousRole);
        boardAccessRepository.save(boardAccess);
    }

    @Override
    public void activateBoardAccessAndChangeRoleByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId,
            Role previousRole)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(
                boardId, userId).orElseThrow(BoardAccessNotFoundException::new);
        boardAccess.setRole(previousRole);
        boardAccess.setUserActive(true);
        boardAccessRepository.save(boardAccess);
    }
}
