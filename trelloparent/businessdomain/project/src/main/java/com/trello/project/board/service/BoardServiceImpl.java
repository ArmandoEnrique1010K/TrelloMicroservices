package com.trello.project.board.service;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.board.dto.response.BoardMembershipResponse;
import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.board.mapper.BoardMembershipResponseMapper;
import com.trello.project.board.mapper.BoardResponseMapper;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.dto.response.UsersByIdResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.service.handler.OperationAddBoardAndBoardAccessUserOwnerService;
import com.trello.project.operation.service.handler.OperationDeleteBoardService;
import com.trello.project.operation.service.processor.OperationAddBoardAndBoardAccessUserOwnerProcessor;
import com.trello.project.operation.service.processor.OperationDeleteBoardProcessor;
import com.trello.project.operation.service.register.OperationRegisterService;
import com.trello.project.service.BoardProjectService;

import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardProjectService boardProjectService;
    private final BoardResponseMapper boardResponseMapper;
    private final BoardMembershipResponseMapper boardMembershipResponseMapper;
    private final IdentityClientService identityClientService;
    private final OperationRegisterService operationRegisterService;
    private final OperationAddBoardAndBoardAccessUserOwnerService operationAddBoardAndBoardAccessUserOwnerService;
    private final OperationAddBoardAndBoardAccessUserOwnerProcessor operationAddBoardAndBoardAccessUserOwnerProcessor;
    private final OperationDeleteBoardService operationDeleteBoardService;
    private final OperationDeleteBoardProcessor operationDeleteBoardProcessor;

    public BoardServiceImpl(BoardProjectService boardProjectService,
            BoardResponseMapper boardResponseMapper,
            BoardMembershipResponseMapper boardMembershipResponseMapper,
            IdentityClientService identityClientService,
            OperationRegisterService operationRegisterService,
            OperationAddBoardAndBoardAccessUserOwnerService operationAddBoardAndBoardAccessUserOwnerService,
            OperationAddBoardAndBoardAccessUserOwnerProcessor operationAddBoardAndBoardAccessUserOwnerProcessor,
            OperationDeleteBoardService operationDeleteBoardService,
            OperationDeleteBoardProcessor operationDeleteBoardProcessor) {
        this.boardProjectService = boardProjectService;
        this.boardResponseMapper = boardResponseMapper;
        this.boardMembershipResponseMapper = boardMembershipResponseMapper;
        this.identityClientService = identityClientService;
        this.operationRegisterService = operationRegisterService;
        this.operationAddBoardAndBoardAccessUserOwnerService = operationAddBoardAndBoardAccessUserOwnerService;
        this.operationAddBoardAndBoardAccessUserOwnerProcessor = operationAddBoardAndBoardAccessUserOwnerProcessor;
        this.operationDeleteBoardService = operationDeleteBoardService;
        this.operationDeleteBoardProcessor = operationDeleteBoardProcessor;
    }

    // TODO: CONTINUAR AQUI
    // @Transactional(rollbackFor = Exception.class)
    @Override
    public BoardResponse createBoardByWorkspaceId(UUID workspaceId, UUID ownerUserId, BoardRequest boardRequest,
            UUID idempotencyKey)
            throws BoardAlreadyExistsException {

        Operation operation = operationRegisterService.registerAddBoardAndBoardAccessUserOwner(workspaceId, ownerUserId,
                boardRequest,
                idempotencyKey);

        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {

            operationAddBoardAndBoardAccessUserOwnerService.applyAddBoardAndBoardAccessUserOwner(operation.getId(),
                    false);
        }

        BoardResponse response = operationAddBoardAndBoardAccessUserOwnerProcessor
                .processAddBoardAndBoardAccessUserOwnerOperation(operation.getId());

        return response;

        // String name = boardRequest.getName();

        // Workspace workspace =
        // workspaceProjectService.findWorkspaceByIdAndOwnerUserId(
        // workspaceId,
        // ownerUserId);

        // if (boardProjectService.existsBoardByWorkspaceIdAndName(workspaceId, name)) {
        // throw new BoardAlreadyExistsException();
        // }

        // Board boardToBoardRequest =
        // boardRequestMapper.boardRequestToBoard(boardRequest);
        // boardToBoardRequest.setWorkspace(workspace);

        // Board savedBoard = boardProjectService.saveBoard(boardToBoardRequest);
        // BoardResponse boardResponse =
        // boardResponseMapper.boardToBoardResponse(savedBoard);

        // // // Guarda los datos en la base de datos del microservicio Workflow
        // // // Solamente los datos necesarios: ID de tablero, Rol (WorkflowRole) e ID
        // de
        // // // usuario
        // // // workflowClientService.addBoardAccess(boardResponse.getId(),
        // // // WorkflowRole.OWNER);

        // // Guardar un tablero
        // // TODO: INVESTIGAR COMO MANEJAR PETICIONES CUANDO EL MICROSERVICIO ESTA
        // CAIDO
        // workflowClientService.addBoardAndBoardAccessUserOwner(savedBoard.getId());

        // return boardResponse;
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
        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(
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

    // @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBoard(UUID ownerUserId, UUID boardId) {

        // Operation operation =
        // operationRegisterService.registerAddBoardAndBoardAccessUserOwner(workspaceId,
        // ownerUserId,
        // boardRequest,
        // idempotencyKey);

        // if (operation.getStatus() == OperationStatus.REGISTERED
        // || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
        // || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {

        // operationAddBoardAndBoardAccessUserOwnerService.applyAddBoardAndBoardAccessUserOwner(operation.getId(),
        // false);
        // }
        // operationAddBoardAndBoardAccessUserOwnerProcessor
        // .processAddBoardAndBoardAccessUserOwnerOperation(operation.getId());

        Operation operation = operationRegisterService.registerDeleteBoard(ownerUserId, boardId);

        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
            operationDeleteBoardService.applyDeleteBoard(operation.getId(), false);
        }

        operationDeleteBoardProcessor.processDeleteBoardOperation(operation.getId());

        // Llamar al endpoint del microservicio Workflow para borrar el tablero
        // boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        // workflowClientService.deleteBoardByIdAndBoardAccessAndTasks(boardId);
    }

    @Override
    public List<UUID> listAllMembersIdsAndInvitationsIdsByBoardId(UUID ownerUserId, UUID boardId) {

        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

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
