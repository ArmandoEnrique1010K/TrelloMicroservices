package com.trello.project.membership.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.membership.dto.request.InvitationRequest;
import com.trello.project.membership.dto.response.BoardInvitationResponse;
import com.trello.project.membership.dto.response.InvitationResponse;
import com.trello.project.membership.dto.response.ReceivedInvitationResponse;
import com.trello.project.membership.exception.InvitationAlreadyExistsException;
import com.trello.project.membership.exception.InvitationConfirmedException;
import com.trello.project.membership.mapper.BoardInvitationResponseMapper;
import com.trello.project.membership.mapper.InvitationRequestMapper;
import com.trello.project.membership.mapper.InvitationResponseMapper;
import com.trello.project.membership.mapper.ReceivedInvitationResponseMapper;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final BoardProjectService boardProjectService;
    private final InvitationRequestMapper invitationRequestMapper;
    private final InvitationProjectService invitationProjectService;
    private final InvitationResponseMapper invitationResponseMapper;
    private final MemberProjectService memberProjectService;
    private final IdentityClientService identityClientService;
    private final BoardInvitationResponseMapper boardInvitationResponseMapper;
    private final ReceivedInvitationResponseMapper receivedInvitationResponseMapper;

    public InvitationServiceImpl(
            BoardProjectService boardProjectService,
            InvitationRequestMapper invitationRequestMapper, InvitationProjectService invitationProjectService,
            InvitationResponseMapper invitationResponseMapper,
            MemberProjectService memberProjectService, IdentityClientService identityClientService,
            BoardInvitationResponseMapper boardInvitationResponseMapper,
            ReceivedInvitationResponseMapper receivedInvitationResponseMapper) {
        this.boardProjectService = boardProjectService;
        this.invitationRequestMapper = invitationRequestMapper;
        this.invitationProjectService = invitationProjectService;
        this.invitationResponseMapper = invitationResponseMapper;
        this.memberProjectService = memberProjectService;
        this.identityClientService = identityClientService;
        this.boardInvitationResponseMapper = boardInvitationResponseMapper;
        this.receivedInvitationResponseMapper = receivedInvitationResponseMapper;
    }

    @Override
    public InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException {

        Board board = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        // System.out.println(board.getId());

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
    public InvitationResponse editInvitation(UUID invitationId, InvitationRequest invitationRequest, UUID ownerUserId) {
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
        invitationProjectService.deleteInvitationByIdAndSenderUserId(invitationId, invitationId);
    }

    @Override
    public void acceptInvitation(UUID invitationId, UUID recipientUserId) {
        Invitation findedInvitation = invitationProjectService.findInvitationByIdAndRecipientUserId(invitationId,
                recipientUserId);

        Board findedBoard = findedInvitation.getBoard();

        if (findedInvitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        findedInvitation.setStatus(Status.ACCEPTED);

        invitationProjectService.saveInvitation(findedInvitation);

        // Agregar usuario receptor como miembro del board
        Member member = new Member();
        member.setUserId(recipientUserId);
        member.setRole(findedInvitation.getRole());
        member.setBoard(findedBoard);

        // Guardar member
        memberProjectService.saveMember(member);
    }

    @Override
    public void declineInvitation(UUID invitationId, UUID recipientUserId) {
        Invitation findedInvitation = invitationProjectService.findInvitationByIdAndRecipientUserId(invitationId,
                recipientUserId);

        if (findedInvitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        findedInvitation.setStatus(Status.UNCONFIRMED);
        invitationProjectService.saveInvitation(findedInvitation);
    }

    // Método para tener los datos de los usuarios por ID
    private Map<UUID, UserResponse> getUsersById(
            List<Invitation> invitations,
            Function<Invitation, UUID> userIdExtractor) {

        Set<UUID> userIds = invitations.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return identityClientService.findUsersByIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        // UserResponse::getId,
                        user -> user.getId(),
                        Function.identity()));
    }

    // Método privado para mapear los datos del usuario obtenido
    // Cuando se tiene las invitaciones enviadas a los distintos usuarios desde un
    // tablero
    private void enrichBoardInvitationWithUserData(List<Invitation> invitations,
            List<BoardInvitationResponse> responses) {

        Map<UUID, UserResponse> usersById = getUsersById(invitations,
                // Invitation::getRecipientUserId
                invitation -> invitation.getRecipientUserId()

        );

        for (int i = 0; i < invitations.size(); i++) {
            Invitation invitation = invitations.get(i);
            responses.get(i).setRecipientUser(
                    mapUser(usersById.get(invitation.getRecipientUserId())));
        }
    }

    // Método privado para mapear los datos del usuario obtenido
    // Cuando se tiene las invitaciones recibidas del usuario autenticado
    private void enrichReceivedInvitationWithUserData(List<Invitation> invitations,
            List<ReceivedInvitationResponse> responses) {

        Map<UUID, UserResponse> usersById = getUsersById(invitations,
                // Invitation::getSenderUserId
                invitation -> invitation.getSenderUserId());

        for (int i = 0; i < invitations.size(); i++) {
            Invitation invitation = invitations.get(i);
            responses.get(i).setSenderUser(
                    mapUser(usersById.get(invitation.getSenderUserId())));
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
}
