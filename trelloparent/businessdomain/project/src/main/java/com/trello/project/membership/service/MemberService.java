package com.trello.project.membership.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.enums.Role;
import com.trello.project.membership.dto.response.MemberResponse;

public interface MemberService {

    // Listar miembros por ID de tablero
    // Accesible por el administrador del tablero y por cualquiera de los miembros
    // (que tenga el rol de Member o Admin)
    List<MemberResponse> listAllMembersByBoardId(UUID boardId, UUID userId);

    // Cambiar rol de miembro (solamente permitido por el administrador del tablero)
    MemberResponse changeRoleMemberById(Role role, UUID memberId, UUID ownerUserId);

    // Eliminar miembro (solamente permitido por el administrador)
    void deleteMember(UUID memberId, UUID ownerUserId);

}
