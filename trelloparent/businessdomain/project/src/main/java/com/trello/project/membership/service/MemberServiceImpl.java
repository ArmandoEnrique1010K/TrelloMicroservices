package com.trello.project.membership.service;

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
import com.trello.project.entities.Board;
import com.trello.project.entities.Member;
import com.trello.project.enums.Role;
import com.trello.project.membership.dto.response.MemberResponse;
import com.trello.project.membership.dto.response.UserMemberResponse;
import com.trello.project.membership.mapper.MemberResponseMapper;
import com.trello.project.membership.mapper.UserMemberResponseMapper;
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

    public MemberServiceImpl(BoardProjectService boardProjectService, MemberProjectService memberProjectService,
            MemberResponseMapper memberResponseMapper, UserMemberResponseMapper userMemberResponseMapper,
            IdentityClientService identityClientService, InvitationProjectService invitationProjectService) {
        this.boardProjectService = boardProjectService;
        this.memberProjectService = memberProjectService;
        this.memberResponseMapper = memberResponseMapper;
        this.userMemberResponseMapper = userMemberResponseMapper;
        this.identityClientService = identityClientService;
        this.invitationProjectService = invitationProjectService;
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

        // Buscar miembro
        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        findedMember.setRole(role);

        Board board = findedMember.getBoard();

        // Borrar invitación si existe
        invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
                board.getId(),
                findedMember.getUserId(),
                ownerUserId);

        Member saveMember = memberProjectService.saveMember(findedMember);
        MemberResponse memberResponse = memberResponseMapper.memberToMemberResponse(saveMember);
        return memberResponse;
    }

    @Override
    public MemberResponse deactivateMember(UUID memberId, UUID ownerUserId) {
        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        // Debe desactivar el campo active
        findedMember.setActive(false);

        // En el caso de que exista alguna invitación relacionada al usuario que se va a
        // eliminar se tiene que eliminar la invitación
        Board board = findedMember.getBoard();

        invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
                board.getId(),
                findedMember.getUserId(),
                ownerUserId);

        Member saveMember = memberProjectService.saveMember(findedMember);
        MemberResponse memberResponse = memberResponseMapper.memberToMemberResponse(saveMember);
        return memberResponse;
    }

    private Map<UUID, UserResponse> getUsersById(
            List<Member> members,
            Function<Member, UUID> userIdExtractor) {

        Set<UUID> userIds = members.stream()
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

    private void enrichUserMemberWithUserData(List<Member> members, List<UserMemberResponse> responses) {

        Map<UUID, UserResponse> usersById = getUsersById(
                members, member -> member.getUserId());

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            responses.get(i).setMemberUser(
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
