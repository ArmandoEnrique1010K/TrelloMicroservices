package com.trello.project.operation.service.register;

import com.trello.project.entities.Operation;

import java.util.UUID;

public interface OperationRegisterService {
    // Métodos que representan registros de operaciones

    // Aceptar invitación
    Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId);
}
