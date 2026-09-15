package com.trello.project.membership.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.membership.dto.request.InvitationRequest;
import com.trello.project.membership.dto.response.InvitationResponse;
import com.trello.project.membership.dto.response.InvitationSenderUserResponse;
import com.trello.project.membership.exception.InvitationAlreadyExistsException;
import com.trello.project.membership.exception.InvitationConfirmedException;
import com.trello.project.membership.mapper.InvitationRequestMapper;
import com.trello.project.membership.mapper.InvitationResponseMapper;
import com.trello.project.membership.mapper.InvitationSenderUserResponseMapper;
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
    private final InvitationSenderUserResponseMapper invitationSenderUserResponseMapper;

    public InvitationServiceImpl(
            BoardProjectService boardProjectService,
            InvitationRequestMapper invitationRequestMapper, InvitationProjectService invitationProjectService,
            InvitationResponseMapper invitationResponseMapper,
            MemberProjectService memberProjectService, IdentityClientService identityClientService,
            InvitationSenderUserResponseMapper invitationSenderUserResponseMapper) {
        this.boardProjectService = boardProjectService;
        this.invitationRequestMapper = invitationRequestMapper;
        this.invitationProjectService = invitationProjectService;
        this.invitationResponseMapper = invitationResponseMapper;
        this.memberProjectService = memberProjectService;
        this.identityClientService = identityClientService;
        this.invitationSenderUserResponseMapper = invitationSenderUserResponseMapper;
    }

    @Override
    public InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException {

        Board board = boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);
        System.out.println(board.getId());

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

        Invitation savedInvitation = invitationProjectService.saveInvitation(invitationToInvitationRequest);
        InvitationResponse invitationResponse = invitationResponseMapper
                .invitationToInvitationResponse(savedInvitation);
        return invitationResponse;
    }

    @Override
    public List<InvitationSenderUserResponse> listAllInvitationsByRecipientUserId(UUID recipientUserId) {
        List<Invitation> listInvitations = invitationProjectService
                .findAllInvitationsByRecipientUserId(recipientUserId);

        List<InvitationSenderUserResponse> responses = invitationSenderUserResponseMapper
                .invitationListToInvitationSenderUserResponseList(listInvitations);

        // Debe enriquecer los datos para mostrar datos del usuario emisor y receptor
        enrichWithUserData(listInvitations, responses);

        return responses;
    }

    @Override
    public List<InvitationResponse> listAllInvitationsByBoardId(UUID boardId, UUID ownerUserId) {
        // Verificar que el usuario que ha iniciado sesion en la aplicacion sea el
        // administrador del tablero
        boardProjectService.findBoardByIdAndOwnerUserId(boardId, ownerUserId);

        List<Invitation> listInvitations = invitationProjectService.findAllInvitationsByBoardId(boardId);
        return invitationResponseMapper.invitationListToInvitationResponseList(listInvitations);
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

    // Método privado para mapear los datos del usuario obtenido
    private void enrichWithUserData(
            List<Invitation> invitations,
            List<InvitationSenderUserResponse> responses) {

        Set<UUID> userIds = invitations.stream()
                .flatMap(invitation -> {
                    Stream<UUID> ids = Stream.of(
                            invitation.getSenderUserId(),
                            invitation.getRecipientUserId());

                    return ids;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<UserResponse> users = identityClientService.findUsersByIds(userIds);

        Map<UUID, UserResponse> usersById = users.stream()
                .collect(Collectors.toMap(
                        UserResponse::getId,
                        Function.identity()));

        for (int i = 0; i < invitations.size(); i++) {

            Invitation invitation = invitations.get(i);
            InvitationSenderUserResponse response = responses.get(i);

            response.setSenderUser(
                    mapUser(usersById.get(
                            invitation.getSenderUserId())));

            response.setRecipientUser(
                    mapUser(usersById.get(
                            invitation.getRecipientUserId())));
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
