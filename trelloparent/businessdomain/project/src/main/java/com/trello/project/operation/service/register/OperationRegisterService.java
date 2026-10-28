package com.trello.project.operation.service.register;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.entities.Operation;
import com.trello.project.enums.Role;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotPendingException;

import java.util.UUID;

public interface OperationRegisterService {
    // Métodos que representan registros de operaciones

    // Aceptar invitación
    Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationConfirmedException, OperationNotPendingException;

    Operation registerChangeRoleMember(Role role, UUID memberId, UUID ownerUserId);

    Operation registerDeactivateMember(UUID memberId, UUID ownerUserId);

    // Crear tablero y permiso de acceso del usuario administrador
    Operation registerAddBoardAndBoardAccessUserOwner(UUID workspaceId, UUID ownerUserId,
            BoardRequest boardRequest,
            UUID idempotencyKey);
}
