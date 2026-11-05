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
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.dto.response.UsersMapResponse;
import com.trello.project.client.dto.response.UsersListResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Operation;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.InvitationNotFoundException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.exception.WorkspaceNotFoundException;
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
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.operation.service.processor.OperationAcceptInvitationProcessor;
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
        this.operationRegisterService = operationRegisterService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
        this.operationAcceptInvitationProcessor = operationAcceptInvitationProcessor;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest)
            throws WorkspaceNotFoundException, BoardNotFoundException, InvitationAlreadyExistsException {

        Board board = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);
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
    public List<BoardInvitationResponse> listAllInvitationsByBoardId(UUID boardId, UUID ownerUserId)
            throws WorkspaceNotFoundException, BoardNotFoundException {
        // Verificar que el usuario que ha iniciado sesion en la aplicacion sea el
        // administrador del tablero
        boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

        List<Invitation> listInvitations = invitationProjectService.findAllInvitationsByBoardId(boardId);
        List<BoardInvitationResponse> responses = boardInvitationResponseMapper
                .invitationListToBoardInvitationResponseList(listInvitations);

        enrichBoardInvitationWithUserData(listInvitations, responses);
        return responses;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InvitationResponse editInvitation(UUID invitationId, InvitationRequest invitationRequest, UUID ownerUserId)
            throws InvitationNotFoundException, InvitationConfirmedException {
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteInvitation(UUID invitationId, UUID ownerUserId)
            throws InvitationNotFoundException {
        invitationProjectService.deleteInvitationByIdAndSenderUserId(invitationId, ownerUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void declineInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationNotFoundException, InvitationConfirmedException {
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public InvitationResponse resendInvitation(UUID boardId, UUID ownerUserId, UUID memberUserId,
            InvitationRequest invitationRequest)
            throws WorkspaceNotFoundException, BoardNotFoundException, MemberNotFoundException,
            InvitationAlreadyExistsException {

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
    public UsersListResponse listAllAvailableUsers(UUID boardId, UUID ownerUserId, String email)
            throws WorkspaceNotFoundException, BoardNotFoundException {
        // Buscar el tablero por ID y usuario administrador del espacio de trabajo
        // Recordar que el administrador del espacio de trabajo es el unico en buscar
        // usuarios disponibles para invitarlos

        // 1. Buscar el tablero
        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

        // Se tiene en cuenta que toma la lista de todos los miembros, incluyendo los
        // miembros inactivos

        // 2. Obtener los IDs de los miembros, incluyendo los inactivos
        List<UUID> memberIds = findedBoard.getMembers()
                .stream()
                .map(member -> member.getUserId())
                .toList();

        // 3. Obtener los IDs de los destinatarios de invitaciones
        List<UUID> invitationIds = findedBoard.getInvitations()
                .stream()
                .map(invitation -> invitation.getRecipientUserId())
                .distinct().toList();

        // 4. Unificar los IDs que deben excluirse
        List<UUID> excludedUsersIds = Stream.concat(memberIds.stream(), invitationIds.stream()).toList();

        // 5. Consultar Identity con tolerancia a fallos
        return fetchAvailableUsers(email, excludedUsersIds);
    }

    // --------------------------------------------------
    // Métodos orientados a la gestión de operaciones
    // --------------------------------------------------

    // Los métodos de registro de operaciones deben ejecutarse sin una
    // transacción externa iniciada por esta clase, para permitir que el
    // registro de la operación se confirme independientemente de las
    // modificaciones de negocio que se realicen posteriormente; dicho de otro modo,
    // no va a llevar la anotación @Transactional en este método

    // TODO: FALTA IMPLEMENTAR
    // Flujo de ejecución para el procesamiento en segundo plano:
    // 1. REGISTER: registrar la operación en la base de datos.
    // 2. HANDLER (modo AUTOMATIC): ejecutar el procesamiento automático.
    // 3. PROCESSOR: procesar la operación según su estado y gestionar
    // la continuación del flujo de la saga.

    @Override
    public void acceptInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationNotFoundException, InvitationConfirmedException, OperationNotPendingException,
            OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            OperationManualReviewException, BusinessRuleException {
        // 1. Registra la operación de aceptación de invitación o recupera
        // una operación existente, según las reglas de registro.

        // Esto permite disponer de una operación persistida que identifique
        // el proceso y permita gestionar su resultado.
        Operation operation = operationRegisterService
                .registerAcceptInvitation(invitationId, recipientUserId);

        // 2. Aplica manualmente los cambios de negocio asociados a la aceptación.
        // El segundo argumento en false indica que la ejecución no se realiza
        // en modo automático.

        // Separar este paso del registro permite conservar la operación
        // aunque falle la aplicación de los cambios de negocio.
        operationAcceptInvitationService.applyAcceptInvitation(operation.getId(), false);

        // 3. Procesa la operación según su estado en el microservicio Workflow.
        // Este paso permite continuar con la coordinación de la saga y
        // determinar las acciones posteriores que correspondan.
        operationAcceptInvitationProcessor.processAcceptInvitationOperation(operation.getId());
    }

    // --------------------------------------
    // Metodos auxiliares
    // --------------------------------------

    // Método auxiliar para hacer una consulta al microservicio Identity
    private UsersListResponse fetchAvailableUsers(
            String email,
            List<UUID> excludedUsersIds) {

        UsersListResponse result = new UsersListResponse();

        try {
            List<UserResponse> users = identityClientService
                    .listAllUsersByEmailAndExcludingIds(email, excludedUsersIds);

            result.setAvailable(true);
            result.setUsersList(users);

        } catch (ServiceUnavailableException e) {

            result.setAvailable(false);
            result.setUsersList(List.of());
        }

        return result;
    }

    // Método privado para mapear los datos del usuario obtenido
    // Cuando se tiene las invitaciones enviadas a los distintos usuarios desde un
    // tablero
    private void enrichBoardInvitationWithUserData(List<Invitation> invitations,
            List<BoardInvitationResponse> responses) {
        UsersMapResponse result = fetchUsersById(
                invitations, invitation -> invitation.getRecipientUserId());

        Map<UUID, UserResponse> usersById = result.getUsersMap();

        for (int i = 0; i < invitations.size(); i++) {
            Invitation invitation = invitations.get(i);

            BoardInvitationResponse response = responses.get(i);

            response.setUserDataAvailable(result.isAvailable());
            response.setRecipientUser(mapUser(usersById.get(invitation.getRecipientUserId())));
        }
    }

    // Método privado para mapear los datos del usuario obtenido
    // Cuando se tiene las invitaciones recibidas del usuario autenticado
    private void enrichReceivedInvitationWithUserData(List<Invitation> invitations,
            List<ReceivedInvitationResponse> responses) {

        UsersMapResponse result = fetchUsersById(
                invitations, invitation -> invitation.getSenderUserId());

        Map<UUID, UserResponse> usersById = result.getUsersMap();

        for (int i = 0; i < invitations.size(); i++) {
            Invitation invitation = invitations.get(i);

            ReceivedInvitationResponse response = responses.get(i);

            response.setUserDataAvailable(result.isAvailable());
            response.setSenderUser(mapUser(usersById.get(invitation.getSenderUserId())));
        }
    }

    // Método para tener los datos de los usuarios por ID
    private UsersMapResponse fetchUsersById(
            List<Invitation> invitations,
            Function<Invitation, UUID> userIdExtractor) {

        Set<UUID> userIds = invitations.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        try {
            Map<UUID, UserResponse> usersById = identityClientService.listAllUsersByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(
                            // UserResponse::getId,
                            user -> user.getId(),
                            Function.identity()));

            UsersMapResponse result = new UsersMapResponse();
            result.setAvailable(true);
            result.setUsersMap(usersById);
            return result;

        } catch (ServiceUnavailableException e) {
            UsersMapResponse result = new UsersMapResponse();
            result.setAvailable(false);

            result.setUsersMap(Map.of());
            return result;
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
}
