package com.trello.project.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.entities.Member;
import com.trello.project.member.exception.MemberNotFoundException;

public interface MemberProjectService {

    // Verificar si el ID del usuario coincide con el ID del administrador del
    // tablero o uno de los miembros del tablero por ID

    // Si es miembro del tablero debe tener el rol de MEMBER o ADMIN
    // boolean existsBoardByOwnerUserIdOrMemberUserId(UUID boardId, UUID userId);
    Member saveMember(Member member);

    List<Member> findAllMembersByBoardIdAndIsOwnerUser(UUID boardId, boolean isOwnerUser);

    Member findMemberByIdAndOwnerUserId(UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException;

    // void deleteMemberById(UUID memberId, UUID ownerUserId) throws
    // MemberNotFoundException
    // ;

}
