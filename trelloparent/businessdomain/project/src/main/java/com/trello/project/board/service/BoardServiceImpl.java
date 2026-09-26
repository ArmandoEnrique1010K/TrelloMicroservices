package com.trello.project.board.service;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.board.mapper.BoardRequestMapper;
import com.trello.project.board.mapper.BoardResponseMapper;
import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Workspace;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.WorkspaceProjectService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardProjectService boardProjectService;
    private final BoardRequestMapper boardRequestMapper;
    private final BoardResponseMapper boardResponseMapper;
    private final WorkspaceProjectService workspaceProjectService;
    private final WorkflowClientService workflowClientService;

    BoardServiceImpl(BoardProjectService boardProjectService, BoardRequestMapper boardRequestMapper,
            BoardResponseMapper boardResponseMapper, WorkspaceProjectService workspaceProjectService,
            WorkflowClientService workflowClientService) {
        this.boardProjectService = boardProjectService;
        this.boardRequestMapper = boardRequestMapper;
        this.boardResponseMapper = boardResponseMapper;
        this.workspaceProjectService = workspaceProjectService;
        this.workflowClientService = workflowClientService;
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

        // Guarda los datos en la base de datos del microservicio Workflow
        // Solamente los datos necesarios: ID de tablero, Rol (WorkflowRole) e ID de
        // usuario
        workflowClientService.addBoardAccess(boardResponse.getId(), WorkflowRole.OWNER);

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
        // Llamar al endpoint del microservicio Workflow para borrar permisos de acceso
        workflowClientService.deleteAllBoardAccessByBoardId(boardId);
    }

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
