package com.trello.project.service;

import com.trello.project.repositories.MemberRepository;
import com.trello.project.repositories.WorkspaceRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Board;
import com.trello.project.enums.Role;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.member.exception.MemberNotFoundException;
import com.trello.project.repositories.BoardRepository;

@Service
public class BoardProjectServiceImpl implements BoardProjectService {

    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BoardRepository boardRepository;

    public BoardProjectServiceImpl(BoardRepository boardRepository, WorkspaceRepository workspaceRepository,
            MemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Board> findAllBoardsByWorkspaceIdAndOwnerUserId(UUID workspaceId, UUID ownerUserId)
            throws WorkspaceNotFoundException {

        workspaceRepository.findByIdAndOwnerUserId(workspaceId, ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    public boolean existsBoardByWorkspaceIdAndName(UUID workspaceId, String name) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.existsByWorkspaceIdAndName(workspaceId, name);
    }

    @Override
    public boolean existsBoardByWorkspaceIdAndNameExcludingId(
            UUID workspaceId,
            String name,
            UUID boardId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(WorkspaceNotFoundException::new);

        return boardRepository.existsByWorkspaceIdAndNameAndIdNot(workspaceId, name, boardId);
    }

    @Override
    public Board findBoardByIdAndOwnerUserId(UUID boardId, UUID ownerUserId)
            throws WorkspaceNotFoundException, BoardNotFoundException {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        workspaceRepository.findByIdAndOwnerUserId(
                board.getWorkspace().getId(),
                ownerUserId).orElseThrow(WorkspaceNotFoundException::new);

        return board;
    }

    @Override
    public Board saveBoard(Board board) {
        return boardRepository.save(board);
    }

    @Override
    public void deleteBoardByIdAndOwnerUserId(UUID boardId,
            UUID ownerUserId) throws WorkspaceNotFoundException, BoardNotFoundException {

        Board board = boardRepository
                .findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        workspaceRepository.findByIdAndOwnerUserId(board.getWorkspace().getId(), ownerUserId)
                .orElseThrow(WorkspaceNotFoundException::new);

        boardRepository.delete(board);
    }

    @Override
    public Board findBoardAccessibleByUser(UUID boardId, UUID userId) throws BoardNotFoundException {
        return boardRepository.findBoardAccessibleByUser(
                boardId,
                userId,
                Set.of(Role.ADMIN, Role.MEMBER)).orElseThrow(BoardNotFoundException::new);
    }

    @Override
    public Board findBoardByIdAndOwnerUserIdAndInactiveUserId(UUID boardId, UUID ownerUserId,
            UUID userId) throws WorkspaceNotFoundException, BoardNotFoundException, MemberNotFoundException {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        // Si uno de los ID de userId, no coincide con el ID del miembro quiere decir
        // que no ha sido un miembro del tablero
        memberRepository.findByBoardIdAndUserIdAndActiveFalse(boardId,
                userId).orElseThrow(MemberNotFoundException::new);

        workspaceRepository.findByIdAndOwnerUserId(
                board.getWorkspace().getId(),
                ownerUserId).orElseThrow(WorkspaceNotFoundException::new);

        return board;
    }
}
