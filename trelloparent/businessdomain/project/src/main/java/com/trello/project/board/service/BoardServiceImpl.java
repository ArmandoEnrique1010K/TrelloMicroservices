package com.trello.project.board.service;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.board.dto.response.BoardMembershipResponse;
import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.board.mapper.BoardMembershipResponseMapper;
import com.trello.project.board.mapper.BoardRequestMapper;
import com.trello.project.board.mapper.BoardResponseMapper;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.dto.response.UsersByIdResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Workspace;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.WorkspaceProjectService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardProjectService boardProjectService;
    private final BoardRequestMapper boardRequestMapper;
    private final BoardResponseMapper boardResponseMapper;
    private final WorkspaceProjectService workspaceProjectService;
    private final WorkflowClientService workflowClientService;
    private final BoardMembershipResponseMapper boardMembershipResponseMapper;
    private final IdentityClientService identityClientService;

    public BoardServiceImpl(BoardProjectService boardProjectService, BoardRequestMapper boardRequestMapper,
            BoardResponseMapper boardResponseMapper, WorkspaceProjectService workspaceProjectService,
            WorkflowClientService workflowClientService, BoardMembershipResponseMapper boardMembershipResponseMapper,
            IdentityClientService identityClientService) {
        this.boardProjectService = boardProjectService;
        this.boardRequestMapper = boardRequestMapper;
        this.boardResponseMapper = boardResponseMapper;
        this.workspaceProjectService = workspaceProjectService;
        this.workflowClientService = workflowClientService;
        this.boardMembershipResponseMapper = boardMembershipResponseMapper;
        this.identityClientService = identityClientService;
    }

    @Transactional(rollbackFor = Exception.class)
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
        // workflowClientService.addBoardAccess(boardResponse.getId(),
        // WorkflowRole.OWNER);

        // Guardar un tablero
        // TODO: INVESTIGAR COMO MANEJAR PETICIONES CUANDO EL MICROSERVICIO ESTA CAIDO
        workflowClientService.addBoardAndBoardAccessUserOwner(savedBoard.getId());

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBoard(UUID ownerUserId, UUID boardId) {
        // Llamar al endpoint del microservicio Workflow para borrar el tablero
        boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        workflowClientService.deleteBoardByIdAndBoardAccessAndTasks(boardId);
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

    @Override
    public List<BoardMembershipResponse> listAllBoardsByMemberUserId(UUID memberUserId) {
        List<Board> listBoards = boardProjectService.findAllBoardsByMemberActiveTrueAndMemberUserId(memberUserId);
        List<BoardMembershipResponse> responses = boardMembershipResponseMapper
                .boardListToBoardMembershipResponseList(
                        listBoards,
                        memberUserId);

        enrichBoardMembershipWithUserData(listBoards, responses);

        return responses;
    }

    // Método para tener los datos de los usuarios por ID
    private UsersByIdResponse getUsersById(
            List<Board> boards,
            Function<Board, UUID> userIdExtractor) {

        Set<UUID> userIds = boards.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        UsersByIdResponse result = new UsersByIdResponse();

        if (userIds.isEmpty()) {
            result.setAvailable(true);
            result.setUsersById(Map.of());
            return result;
        }

        try {
            Map<UUID, UserResponse> usersById = identityClientService.findUsersByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(
                            user -> user.getId(),
                            Function.identity()));
            result.setAvailable(true);
            result.setUsersById(usersById);

        } catch (ServiceUnavailableException e) {
            result.setAvailable(false);
            result.setUsersById(Map.of());
        }

        return result;
    }

    private void enrichBoardMembershipWithUserData(
            List<Board> boards,
            List<BoardMembershipResponse> responses) {

        UsersByIdResponse result = getUsersById(
                boards, board -> board.getWorkspace().getOwnerUserId());

        Map<UUID, UserResponse> usersById = result.getUsersById();

        for (int i = 0; i < boards.size(); i++) {
            Board board = boards.get(i);

            BoardMembershipResponse response = responses.get(i);
            UUID ownerUserId = board.getWorkspace().getOwnerUserId();

            // Establece la disponibilidad de la data y el usuario propietario del espacio
            // de trabajo
            response.setUserDataAvailable(result.isAvailable());
            response.setOwnerUser(mapUser(usersById.get(ownerUserId)));
        }
    }

    private UserResponse mapUser(
            UserResponse user) {

        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());

        return response;
    }
}
