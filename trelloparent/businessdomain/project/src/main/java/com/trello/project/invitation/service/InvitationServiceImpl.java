package com.trello.project.invitation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.dto.response.UsersByIdResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.invitation.dto.request.InvitationRequest;
import com.trello.project.invitation.dto.response.BoardInvitationResponse;
import com.trello.project.invitation.dto.response.InvitationResponse;
import com.trello.project.invitation.dto.response.ReceivedInvitationResponse;
import com.trello.project.invitation.exception.InvitationAlreadyExistsException;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.invitation.mapper.BoardInvitationResponseMapper;
import com.trello.project.invitation.mapper.InvitationRequestMapper;
import com.trello.project.invitation.mapper.InvitationResponseMapper;
import com.trello.project.invitation.mapper.ReceivedInvitationResponseMapper;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.operation.service.processor.OperationAcceptInvitationProcessor;
import com.trello.project.operation.service.processor.OperationProcessor;
import com.trello.project.operation.service.register.OperationRegisterService;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final BoardProjectService boardProjectService;
    private final InvitationRequestMapper invitationRequestMapper;
    private final InvitationProjectService invitationProjectService;
    private final InvitationResponseMapper invitationResponseMapper;
    private final IdentityClientService identityClientService;
    private final BoardInvitationResponseMapper boardInvitationResponseMapper;
    private final ReceivedInvitationResponseMapper receivedInvitationResponseMapper;
    private final OperationProcessor operationProcessor;
    private final OperationRegisterService operationRegisterService;
    private final OperationAcceptInvitationService operationAcceptInvitationService;
    private final OperationAcceptInvitationProcessor operationAcceptInvitationProcessor;

    public InvitationServiceImpl(
            BoardProjectService boardProjectService,
            InvitationRequestMapper invitationRequestMapper, InvitationProjectService invitationProjectService,
            InvitationResponseMapper invitationResponseMapper,
            IdentityClientService identityClientService,
            BoardInvitationResponseMapper boardInvitationResponseMapper,
            ReceivedInvitationResponseMapper receivedInvitationResponseMapper,
            OperationProcessor operationProcessor,
            OperationRegisterService operationRegisterService,
            OperationAcceptInvitationService operationAcceptInvitationService,
            OperationAcceptInvitationProcessor operationAcceptInvitationProcessor) {
        this.boardProjectService = boardProjectService;
        this.invitationRequestMapper = invitationRequestMapper;
        this.invitationProjectService = invitationProjectService;
        this.invitationResponseMapper = invitationResponseMapper;
        this.identityClientService = identityClientService;
        this.boardInvitationResponseMapper = boardInvitationResponseMapper;
        this.receivedInvitationResponseMapper = receivedInvitationResponseMapper;
        this.operationProcessor = operationProcessor;
        this.operationRegisterService = operationRegisterService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
        this.operationAcceptInvitationProcessor = operationAcceptInvitationProcessor;
    }

    @Override
    public InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException {

        Board board = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        // System.out.println(board.getId());

        // Si hay una invitación rechazada, no podra volver a enviar la invitación a
        // menos de que haya eliminado la invitación del sistema
        if (invitationProjectService.existsInvitationByBoardIdAndRecipientUserId(boardId, recipientUserId)) {
            throw new InvitationAlreadyExistsException();
        }

        Invitation invitationToInvitationRequest = invitationRequestMapper
                .invitationRequestToInvitation(invitationRequest);

        // Emisor
        invitationToInvitationRequest.setSenderUserId(ownerUserId);
        // Receptor
        invitationToInvitationRequest.setRecipientUserId(recipientUserId);

        invitationToInvitationRequest.setBoard(board);
        invitationToInvitationRequest.setStatus(Status.UNCONFIRMED);
        invitationToInvitationRequest.setSendedAt(LocalDateTime.now());

        Invitation savedInvitation = invitationProjectService.saveInvitation(invitationToInvitationRequest);

        InvitationResponse invitationResponse = invitationResponseMapper
                .invitationToInvitationResponse(savedInvitation);
        return invitationResponse;
    }

    @Override
    public List<ReceivedInvitationResponse> listAllInvitationsByRecipientUserId(UUID recipientUserId) {
        List<Invitation> listInvitations = invitationProjectService
                .findAllUnconfirmedInvitationsByRecipientUserId(recipientUserId);

        List<ReceivedInvitationResponse> responses = receivedInvitationResponseMapper
                .invitationListToReceivedInvitationResponseList(listInvitations);

        // Debe enriquecer los datos para mostrar datos del usuario emisor
        enrichReceivedInvitationWithUserData(listInvitations, responses);

        return responses;
    }

    @Override
    public List<BoardInvitationResponse> listAllInvitationsByBoardId(UUID boardId, UUID ownerUserId) {
        // Verificar que el usuario que ha iniciado sesion en la aplicacion sea el
        // administrador del tablero
        boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);

        List<Invitation> listInvitations = invitationProjectService.findAllInvitationsByBoardId(boardId);
        List<BoardInvitationResponse> responses = boardInvitationResponseMapper
                .invitationListToBoardInvitationResponseList(listInvitations);

        enrichBoardInvitationWithUserData(listInvitations, responses);
        return responses;
    }

    @Override
    public InvitationResponse editInvitation(UUID invitationId, InvitationRequest invitationRequest, UUID ownerUserId)
            throws InvitationConfirmedException {
        String message = invitationRequest.getMessage();
        Role role = invitationRequest.getRole();

        // Cuando busca la invitación debe asegurarse de que tambien busque por el
        // usuario emisor
        Invitation findedInvitation = invitationProjectService.findInvitationByIdAndSenderUserId(invitationId,
                ownerUserId);

        // Si la invitación ha sido aceptada o rechazada por el usuario receptor
        if (findedInvitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        findedInvitation.setMessage(message);
        findedInvitation.setRole(role);
        findedInvitation.setSendedAt(LocalDateTime.now());

        Invitation saveInvitation = invitationProjectService.saveInvitation(findedInvitation);
        InvitationResponse invitationResponse = invitationResponseMapper.invitationToInvitationResponse(saveInvitation);
        return invitationResponse;
    }

    @Override
    public void deleteInvitation(UUID invitationId, UUID ownerUserId) {
        invitationProjectService.deleteInvitationByIdAndSenderUserId(invitationId, ownerUserId);
    }

    @Override
    public void declineInvitation(UUID invitationId, UUID recipientUserId) {
        Invitation findedInvitation = invitationProjectService.findInvitationByIdAndRecipientUserId(invitationId,
                recipientUserId);

        if (findedInvitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // Marcar la invitación como rechazada
        // El administrador del espacio de trabajo podra verlo que ha sido rechazado
        findedInvitation.setStatus(Status.REJECTED);
        invitationProjectService.saveInvitation(findedInvitation);
    }

    @Override
    public InvitationResponse resendInvitation(UUID boardId, UUID ownerUserId, UUID memberUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException {

        // Buscar si el usuario receptor esta como miembro inactivo en el tablero
        Board board = boardProjectService.findBoardByIdAndOwnerUserIdAndInactiveUserId(boardId, ownerUserId,
                memberUserId);

        // El usuario receptor ahora es el miembro del usuario
        // Si existe una invitación, entonces no se debe volver a enviar la invitación
        // por segunda vez
        if (invitationProjectService.existsInvitationByBoardIdAndRecipientUserId(boardId, memberUserId)) {
            throw new InvitationAlreadyExistsException();
        }

        Invitation invitationToInvitationRequest = invitationRequestMapper
                .invitationRequestToInvitation(invitationRequest);

        invitationToInvitationRequest.setSenderUserId(ownerUserId);
        invitationToInvitationRequest.setRecipientUserId(memberUserId);

        invitationToInvitationRequest.setBoard(board);
        invitationToInvitationRequest.setStatus(Status.UNCONFIRMED);
        invitationToInvitationRequest.setSendedAt(LocalDateTime.now());

        Invitation savedInvitation = invitationProjectService.saveInvitation(invitationToInvitationRequest);
        InvitationResponse invitationResponse = invitationResponseMapper
                .invitationToInvitationResponse(savedInvitation);
        return invitationResponse;

    }

    @Override
    public List<UserResponse> listAllAvailableUsers(UUID boardId, UUID ownerUserId, String email) {
        // Buscar el tablero por ID y usuario administrador del espacio de trabajo
        // Recordar que el administrador del espacio de trabajo es el unico en buscar
        // usuarios disponibles para invitarlos

        // Se excluye de la lista desde el controlador del microservicio Identity
        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        List<UUID> memberIds = findedBoard.getMembers()
                .stream()
                .map(member -> member.getUserId())
                .toList();

        List<UUID> invitationIds = findedBoard.getInvitations()
                .stream()
                .map(invitation -> invitation.getRecipientUserId())
                .distinct().toList();

        List<UUID> excludedUsersIds = Stream.concat(memberIds.stream(), invitationIds.stream()).toList();

        // Llamado al servicio del cliente
        List<UserResponse> response = identityClientService.listAllUsersByEmailAndExcludingIds(email,
                excludedUsersIds);

        return response;
    }

    // ----------------------------------------
    // A partir de aqui los metodos estan orientado a operaciones
    // ----------------------------------------

    // Los métodos que van a escribir una opaeración en la base de datos no deben
    // llevar @Transactional en esta clase

    // NOTA: PARA EJECUTAR LAS TAREAS EN SEGUNDO PLANO DEBE EJECUTAR:
    // REGISTER
    // HANDLER EN MODO AUTOMATIC
    // PROCESSOR

    @Override
    public void acceptInvitation(UUID invitationId, UUID recipientUserId) {
        // 1. Registrar o recuperar la operación.
        Operation operation = operationRegisterService
                .registerAcceptInvitation(invitationId, recipientUserId);

        // 2. Si la compensación local ya terminó, permitir que el usuario
        // intente completar la compensación remota.
        // if (operation.getStatus() == OperationStatus.COMPENSATED) {
        // operationProcessor.processOperation(operation.getId());
        // return;
        // }

        // 3. Aplicar cambios locales únicamente si corresponde
        // iniciar una operación nueva o repetir una ya compensada.
        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {

            // Ejecuta la operación de forma manual
            // Recordar que el método de acceptInvitation se llama desde un endpoint
            operationAcceptInvitationService
                    .applyAcceptInvitation(operation.getId(), false);
        }

        // 4. Procesar según el estado persistido.
        // operationProcessor.processOperation(operation.getId());
        operationAcceptInvitationProcessor.processAcceptInvitationOperation(operation.getId());

    }

    // --------------------------------------
    // Metodos auxiliares
    // --------------------------------------

    // Método para tener los datos de los usuarios por ID
    private UsersByIdResponse getUsersById(
            List<Invitation> invitations,
            Function<Invitation, UUID> userIdExtractor) {

        Set<UUID> userIds = invitations.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        try {
            Map<UUID, UserResponse> usersById = identityClientService.findUsersByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(
                            // UserResponse::getId,
                            user -> user.getId(),
                            Function.identity()));

            UsersByIdResponse result = new UsersByIdResponse();
            result.setAvailable(true);
            result.setUsersById(usersById);
            return result;

        } catch (ServiceUnavailableException e) {
            UsersByIdResponse result = new UsersByIdResponse();
            result.setAvailable(false);

            result.setUsersById(Map.of());
            return result;
        }
    }

    // Método privado para mapear los datos del usuario obtenido
    // Cuando se tiene las invitaciones enviadas a los distintos usuarios desde un
    // tablero
    private void enrichBoardInvitationWithUserData(List<Invitation> invitations,
            List<BoardInvitationResponse> responses) {
        UsersByIdResponse result = getUsersById(
                invitations, invitation -> invitation.getRecipientUserId());

        Map<UUID, UserResponse> usersById = result.getUsersById();

        for (int i = 0; i < invitations.size(); i++) {
            Invitation invitation = invitations.get(i);

            BoardInvitationResponse response = responses.get(i);

            response.setUserDataAvailable(result.isAvailable());
            response.setRecipientUser(mapUser(usersById.get(invitation.getRecipientUserId())));
        }
    }

    // Metodo para mapear los campos de UserResponse
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

    // Método privado para mapear los datos del usuario obtenido
    // Cuando se tiene las invitaciones recibidas del usuario autenticado
    private void enrichReceivedInvitationWithUserData(List<Invitation> invitations,
            List<ReceivedInvitationResponse> responses) {

        UsersByIdResponse result = getUsersById(
                invitations, invitation -> invitation.getSenderUserId());

        Map<UUID, UserResponse> usersById = result.getUsersById();

        for (int i = 0; i < invitations.size(); i++) {
            Invitation invitation = invitations.get(i);

            ReceivedInvitationResponse response = responses.get(i);

            response.setUserDataAvailable(result.isAvailable());
            response.setSenderUser(mapUser(usersById.get(invitation.getSenderUserId())));
        }
    }

}
