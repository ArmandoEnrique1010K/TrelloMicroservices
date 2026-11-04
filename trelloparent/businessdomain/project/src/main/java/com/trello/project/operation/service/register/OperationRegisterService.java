package com.trello.project.operation.service.register;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.entities.Operation;
import com.trello.project.enums.Role;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.InvitationNotFoundException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotPendingException;

import java.util.UUID;

public interface OperationRegisterService {
    // Métodos que representan registros de operaciones

    // Aceptar invitación
    Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationNotFoundException, InvitationConfirmedException, OperationNotPendingException;

    Operation registerChangeRoleMember(Role role, UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException, MemberInactiveException, OperationNotPendingException;

    Operation registerDeactivateMember(UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException, MemberInactiveException, OperationNotPendingException;

    // Crear tablero y permiso de acceso del usuario administrador en el
    // microservicio Workflow
    Operation registerCreateBoard(UUID workspaceId, UUID ownerUserId,
            BoardRequest boardRequest,
            UUID idempotencyKey)
            throws WorkspaceNotFoundException, OperationNotPendingException;

    Operation registerDeleteBoard(UUID ownerUserId, UUID boardId)
            throws WorkspaceNotFoundException, BoardNotFoundException, OperationNotPendingException;

    Operation registerDeleteWorkspace(UUID ownerUserId, UUID workspaceId)
            throws WorkspaceNotFoundException, OperationNotPendingException;
}
