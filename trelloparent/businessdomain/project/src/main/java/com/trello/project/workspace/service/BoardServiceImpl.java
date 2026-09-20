package com.trello.project.workspace.service;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Workspace;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.WorkspaceProjectService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.trello.project.workspace.dto.request.BoardRequest;
import com.trello.project.workspace.dto.response.BoardResponse;
import com.trello.project.workspace.exception.BoardAlreadyExistsException;
import com.trello.project.workspace.mapper.BoardRequestMapper;
import com.trello.project.workspace.mapper.BoardResponseMapper;

@Service
public class BoardServiceImpl implements BoardService {

    private final IdentityClientService identityClientService;
    private final BoardProjectService boardProjectService;
    private final BoardRequestMapper boardRequestMapper;
    private final BoardResponseMapper boardResponseMapper;
    private final WorkspaceProjectService workspaceProjectService;

    BoardServiceImpl(BoardProjectService boardProjectService, BoardRequestMapper boardRequestMapper,
            BoardResponseMapper boardResponseMapper, WorkspaceProjectService workspaceProjectService,
            IdentityClientService identityClientService) {
        this.boardProjectService = boardProjectService;
        this.boardRequestMapper = boardRequestMapper;
        this.boardResponseMapper = boardResponseMapper;
        this.workspaceProjectService = workspaceProjectService;
        this.identityClientService = identityClientService;
    }

    @Override
    public BoardResponse createBoardByWorkspaceId(UUID workspaceId, UUID ownerUserId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException {

        String name = boardRequest.getName();

        Workspace workspace = workspaceProjectService.findWorkspaceByIdAndOwnerUserId(
                workspaceId,
                ownerUserId);

        if (boardProjectService.existsBoardByWorkspaceIdAndName(workspaceId, name)) {
            throw new BoardAlreadyExistsException();
        }

        Board boardToBoardRequest = boardRequestMapper.boardRequestToBoard(boardRequest);
        boardToBoardRequest.setWorkspace(workspace);

        Board savedBoard = boardProjectService.saveBoard(boardToBoardRequest);
        BoardResponse boardResponse = boardResponseMapper.boardToBoardResponse(savedBoard);

        return boardResponse;
    }

    @Override
    public List<BoardResponse> listAllBoardsByWorkspaceId(UUID workspaceId, UUID ownerUserId) {
        List<Board> listBoardsByWorkspaceId = boardProjectService.findAllBoardsByWorkspaceIdAndOwnerUserId(workspaceId,
                ownerUserId);
        return boardResponseMapper.boardListToBoardResponseList(listBoardsByWorkspaceId);
    }

    @Override
    public BoardResponse editBoard(UUID ownerUserId, UUID boardId, BoardRequest boardRequest)
            throws BoardAlreadyExistsException {

        String name = boardRequest.getName();
        String description = boardRequest.getDescription();

        // Buscar tablero
        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserId(
                boardId,
                ownerUserId);

        UUID workspaceId = findedBoard.getWorkspace().getId();

        if (boardProjectService.existsBoardByWorkspaceIdAndNameExcludingId(workspaceId, name, boardId)) {
            throw new BoardAlreadyExistsException();
        }

        findedBoard.setName(name);
        findedBoard.setDescription(description);

        Board savedBoard = boardProjectService.saveBoard(findedBoard);

        BoardResponse boardResponse = boardResponseMapper.boardToBoardResponse(savedBoard);
        return boardResponse;
    }

    @Override
    public void deleteBoard(UUID ownerUserId, UUID boardId) {
        boardProjectService.deleteBoardByIdAndOwnerUserId(boardId, ownerUserId);
    }

    // TODO: CONTINUAR AQUI, METODO PARA LISTAR LOS IDS DE LOS MIEMBROS E INVITADOS
    @Override
    public List<UUID> listAllMembersIdsAndInvitationsIdsByBoardId(UUID ownerUserId, UUID boardId) {

        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);

        List<UUID> memberIds = findedBoard.getMembers()
                .stream()
                .map(member -> member.getUserId())
                .toList();

        List<UUID> invitationIds = findedBoard.getInvitations()
                .stream()
                .map(invitation -> invitation.getRecipientUserId())
                .distinct().toList();

        return Stream.concat(
                memberIds.stream(),
                invitationIds.stream()).toList();
    }

}
