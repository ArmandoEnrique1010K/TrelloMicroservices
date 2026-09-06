package com.trello.project.service;

import com.trello.project.repositories.WorkspaceRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Board;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.repositories.BoardRepository;

@Service
public class BoardProjectServiceImpl implements BoardProjectService {

    private final WorkspaceRepository workspaceRepository;
    private final BoardRepository boardRepository;

    public BoardProjectServiceImpl(BoardRepository boardRepository, WorkspaceRepository workspaceRepository) {
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public List<Board> findAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException {

        workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public boolean existsBoardByWorkspaceIdAndName(UUID workspaceId, UUID ownerUserId, String name)
            throws WorkspaceNotFoundException {
        workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.existsByWorkspaceIdAndName(workspaceId, name);
    }

    @Override
    public boolean existsBoardByWorkspaceIdAndNameExcludingId(
            UUID workspaceId,
            String name,
            UUID ownerUserId,
            UUID boardId) throws WorkspaceNotFoundException {

        workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.existsByWorkspaceIdAndNameAndIdNot(workspaceId, name, boardId);
    }

    @Override
    public Board findBoardByIdAndWorkspaceId(UUID boardId, UUID workspaceId,
            UUID ownerUserId) throws WorkspaceNotFoundException, BoardNotFoundException {
        workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        Board board = boardRepository.findByIdAndWorkspaceId(boardId, workspaceId)
                .orElseThrow(BoardNotFoundException::new);

        return board;
    }

    @Override
    public Board saveBoard(Board board, UUID ownerUserId) throws WorkspaceNotFoundException {

        workspaceRepository.findByIdAndOwnerUserId(board.getWorkspace().getId(), ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.save(board);
    }

    @Override
    public void deleteBoardByIdAndWorkspaceId(UUID boardId, UUID workspaceId,
            UUID ownerUserId) throws WorkspaceNotFoundException, BoardNotFoundException {

        workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        Board board = boardRepository
                .findByIdAndWorkspaceId(boardId, workspaceId)
                .orElseThrow(BoardNotFoundException::new);

        boardRepository.delete(board);
    }
}
