package com.trello.project.member.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.enums.Role;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.member.dto.response.UserMemberResponse;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface MemberService {

    // Listar miembros por ID de tablero
    // Accesible por el administrador del tablero y por cualquiera de los miembros
    // (que tenga el rol de Member o Admin)
    List<UserMemberResponse> listAllMembersByBoardId(UUID boardId, UUID userId)
            throws BoardNotFoundException;

    // SAGA PATTERN
    // Cambiar rol de miembro (solamente permitido por el administrador del tablero)
    MemberResponse changeRoleMemberById(Role role, UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException, MemberInactiveException, OperationNotPendingException,
            OperationNotFoundException, OperationConflictException, OperationManualReviewException,
            OperationCompensatedException, BusinessRuleException;

    // SAGA PATTERN
    // Desactivar miembro (solamente permitido por el administrador)
    // Borrado logico (desactiva al usuario)
    void deactivateMember(UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException, MemberInactiveException, OperationNotPendingException,
            OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, OperationManualReviewException;
}
