package com.trello.project.member.service;

import com.trello.project.client.services.IdentityClientService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.client.dto.response.UsersByIdResponse;
import com.trello.project.entities.Board;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.member.dto.response.UserMemberResponse;
import com.trello.project.member.mapper.MemberResponseMapper;
import com.trello.project.member.mapper.UserMemberResponseMapper;
import com.trello.project.operation.service.handler.OperationChangeRoleMemberService;
import com.trello.project.operation.service.handler.OperationDeactivateMemberService;
import com.trello.project.operation.service.processor.OperationChangeRoleMemberProcessor;
import com.trello.project.operation.service.processor.OperationDeactivateMemberProcessor;
import com.trello.project.operation.service.register.OperationRegisterService;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;

@Service
public class MemberServiceImpl implements MemberService {

    private final IdentityClientService identityClientService;
    private final MemberResponseMapper memberResponseMapper;
    private final UserMemberResponseMapper userMemberResponseMapper;
    private final BoardProjectService boardProjectService;
    private final MemberProjectService memberProjectService;
    private final InvitationProjectService invitationProjectService;
    private final OperationRegisterService operationRegisterService;
    private final OperationChangeRoleMemberService operationChangeRoleMemberService;
    private final OperationChangeRoleMemberProcessor operationChangeRoleMemberProcessor;
    private final OperationDeactivateMemberService operationDeactivateMemberService;
    private final OperationDeactivateMemberProcessor operationDeactivateMemberProcessor;

    public MemberServiceImpl(BoardProjectService boardProjectService, MemberProjectService memberProjectService,
            MemberResponseMapper memberResponseMapper, UserMemberResponseMapper userMemberResponseMapper,
            IdentityClientService identityClientService, InvitationProjectService invitationProjectService,
            OperationRegisterService operationRegisterService,
            OperationChangeRoleMemberService operationChangeRoleMemberService,
            OperationChangeRoleMemberProcessor operationChangeRoleMemberProcessor,
            OperationDeactivateMemberService operationDeactivateMemberService,
            OperationDeactivateMemberProcessor operationDeactivateMemberProcessor) {
        this.boardProjectService = boardProjectService;
        this.memberProjectService = memberProjectService;
        this.memberResponseMapper = memberResponseMapper;
        this.userMemberResponseMapper = userMemberResponseMapper;
        this.identityClientService = identityClientService;
        this.invitationProjectService = invitationProjectService;
        this.operationRegisterService = operationRegisterService;
        this.operationChangeRoleMemberService = operationChangeRoleMemberService;
        this.operationChangeRoleMemberProcessor = operationChangeRoleMemberProcessor;
        this.operationDeactivateMemberService = operationDeactivateMemberService;
        this.operationDeactivateMemberProcessor = operationDeactivateMemberProcessor;
    }

    @Override
    public List<UserMemberResponse> listAllMembersByBoardId(UUID boardId, UUID userId) {
        // Si se trata de un miembro con el rol de VIEWER, no tendra acceso a la lista
        // de los usuarios
        Board board = boardProjectService.findBoardAccessibleByUser(boardId, userId);

        UUID ownerUserId = board.getWorkspace().getOwnerUserId();

        // Verifica si se trata del usuario administrador del espacio de trabajo
        // boolean isOwnerUser = ownerUserId == userId; // ERROR

        // Forma correcta de comparar los UUIDs
        boolean isOwnerUser = ownerUserId.equals(userId);

        // Si es el usuario administrador del tablero, debe incluir los usuarios
        // inactivos, de lo contrario solamente los usuarios activos
        List<Member> listMembersByBoardId = memberProjectService.findAllMembersByBoardIdAndIsOwnerUser(boardId,
                isOwnerUser);
        List<UserMemberResponse> responses = userMemberResponseMapper
                .memberListToUserMemberResponseList(listMembersByBoardId);

        // Método auxiliar para mapear los datos
        enrichUserMemberWithUserData(listMembersByBoardId, responses);
        return responses;

    }

    @Override
    public MemberResponse changeRoleMemberById(Role role, UUID memberId, UUID ownerUserId) {

        Operation operation = operationRegisterService.registerChangeRoleMember(role, memberId, ownerUserId);

        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {

            operationChangeRoleMemberService
                    .applyChangeRoleMember(operation.getId(), false);
        }

        // Este método devuelve un MemberResponse si todo sale bien
        MemberResponse response = operationChangeRoleMemberProcessor
                .processChangeRoleMemberOperation(operation.getId());

        return response;
    }

    @Override
    public void deactivateMember(UUID memberId, UUID ownerUserId) {
        Operation operation = operationRegisterService.registerDeactivateMember(memberId, ownerUserId);

        if (operation.getStatus() == OperationStatus.REGISTERED
                || operation.getStatus() == OperationStatus.LOCAL_COMPENSATION_COMPLETED
                || operation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {

            operationDeactivateMemberService
                    .applyDeactivateMember(operation.getId(), false);
        }

        operationDeactivateMemberProcessor
                .processDeactivateMemberOperation(operation.getId());

        // Member findedMember =
        // memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        // // Debe desactivar el campo active
        // findedMember.setActive(false);

        // // En el caso de que exista alguna invitación relacionada al usuario que se
        // va a
        // // eliminar se tiene que eliminar la invitación
        // Board board = findedMember.getBoard();

        // invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
        // board.getId(),
        // findedMember.getUserId(),
        // ownerUserId);

        // Member saveMember = memberProjectService.saveMember(findedMember);

        // MemberResponse memberResponse =
        // memberResponseMapper.memberToMemberResponse(saveMember);
        // return memberResponse;
    }

    private UsersByIdResponse getUsersById(
            List<Member> members,
            Function<Member, UUID> userIdExtractor) {

        Set<UUID> userIds = members.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Si al llamar al endpoint del microservicio, se obtiene una excepción
        // ServiceUnavailableException, se devuelve una lista vacia
        try {
            Map<UUID, UserResponse> usersById = identityClientService.findUsersByIds(userIds)
                    .stream()
                    .collect(Collectors.toMap(
                            user -> user.getId(),
                            Function.identity()));

            UsersByIdResponse result = new UsersByIdResponse();
            result.setAvailable(true);
            result.setUsersById(usersById);
            return result;

        } catch (ServiceUnavailableException e) {
            UsersByIdResponse result = new UsersByIdResponse();
            result.setAvailable(false);

            // No puede ser null
            result.setUsersById(Map.of());
            return result;
        }
    }

    private void enrichUserMemberWithUserData(List<Member> members, List<UserMemberResponse> responses) {

        UsersByIdResponse result = getUsersById(
                members, member -> member.getUserId());

        Map<UUID, UserResponse> usersById = result.getUsersById();

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            UserMemberResponse response = responses.get(i);

            // Se establece si hay datos de usuario disponible
            response.setUserDataAvailable(result.isAvailable());
            response.setMemberUser(
                    mapUser(usersById.get(member.getUserId())));
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
