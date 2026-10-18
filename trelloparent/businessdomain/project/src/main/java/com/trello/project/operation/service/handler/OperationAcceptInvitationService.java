package com.trello.project.operation.service.handler;

import java.util.UUID;

import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationAcceptInvitationService {
    // Métodos atomicos que representan acciones relacionadas al metodo de
    // "AcceptInvitation"

    // Aplicar acciones en la base de datos
    void applyAcceptInvitation(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, InvitationConfirmedException;

    // Revertir acciones realizadas en la base de datos
    void revertAcceptInvitation(UUID operationId)
            throws OperationNotFoundException;
}
