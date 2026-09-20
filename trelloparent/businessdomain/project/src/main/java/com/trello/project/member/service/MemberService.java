package com.trello.project.member.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.enums.Role;
import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.member.dto.response.UserMemberResponse;

public interface MemberService {

    // Listar miembros por ID de tablero
    // Accesible por el administrador del tablero y por cualquiera de los miembros
    // (que tenga el rol de Member o Admin)
    List<UserMemberResponse> listAllMembersByBoardId(UUID boardId, UUID userId);

    // Cambiar rol de miembro (solamente permitido por el administrador del tablero)
    MemberResponse changeRoleMemberById(Role role, UUID memberId, UUID ownerUserId);

    // Desactivar miembro (solamente permitido por el administrador)
    // Borrado logico (desactiva al usuario)
    MemberResponse deactivateMember(UUID memberId, UUID ownerUserId);

}
