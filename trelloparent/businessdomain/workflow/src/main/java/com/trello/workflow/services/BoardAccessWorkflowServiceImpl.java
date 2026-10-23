package com.trello.workflow.services;

import com.trello.workflow.repositories.BoardAccessRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BoardAccessNotFoundException;

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
    public BoardAccess findBoardAccessByBoardIdAndUserIdAndUserActive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository
                .findByBoardIdAndUserIdAndUserActiveTrueAndStatusTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public boolean existsBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId) {
        return boardAccessRepository.existsByBoardIdAndUserIdAndStatusTrue(boardId, userId);
    }

    @Override
    public BoardAccess findBoardAccessByBoardIdAndUserIdAndUserInactive(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository
                .findByBoardIdAndUserIdAndUserActiveFalseAndStatusTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        return boardAccess;
    }

    @Override
    public void deleteBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(boardId, userId)
                .orElseThrow(BoardAccessNotFoundException::new);
        boardAccessRepository.delete(boardAccess);
    }

    @Override
    public void inactivateBoardAccessAndChangeRoleByBoardIdAndUserId(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(
                boardId, userId).orElseThrow(BoardAccessNotFoundException::new);

        boardAccess.setRole(previousRole);
        boardAccess.setUserActive(false);
        boardAccessRepository.save(boardAccess);
    }

    @Override
    public void changeBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId, Role previousRole)
            throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(
                boardId, userId).orElseThrow(BoardAccessNotFoundException::new);

        boardAccess.setRole(previousRole);
        boardAccessRepository.save(boardAccess);
    }

    @Override
    public void activateBoardAccessByBoardIdAndUserId(UUID boardId, UUID userId) throws BoardAccessNotFoundException {
        BoardAccess boardAccess = boardAccessRepository.findByBoardIdAndUserIdAndStatusTrue(
                boardId, userId).orElseThrow(BoardAccessNotFoundException::new);

        boardAccess.setUserActive(true);
        boardAccessRepository.save(boardAccess);
    }

    // @Override
    // public void disableAllBoardAccessStatusByBoardId(UUID boardId) {
    // List<BoardAccess> boardAcceses =
    // boardAccessRepository.findAllByBoardIdAndStatusTrue(boardId);

    // if (boardAcceses.isEmpty()) {
    // return;
    // }

    // boardAcceses.forEach(boardAccess -> boardAccess.setStatus(false));
    // boardAccessRepository.saveAll(boardAcceses);
    // }

    @Override
    public void enableAllBoardAccessStatusByBoardId(UUID boardId) {
        List<BoardAccess> boardAcceses = boardAccessRepository.findAllByBoardIdAndStatusFalse(boardId);

        if (boardAcceses.isEmpty()) {
            return;
        }

        boardAcceses.forEach(boardAccess -> boardAccess.setStatus(true));
        boardAccessRepository.saveAll(boardAcceses);
    }
}
