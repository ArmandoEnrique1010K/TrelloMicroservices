package com.trello.project.operation.service.handler;

import java.util.UUID;

public interface OperationAcceptInvitationService {
    // Métodos atomicos que representan acciones relacionadas al metodo de
    // "AcceptInvitation"

    // Aplicar acciones en la base de datos
    void applyAcceptInvitation(UUID operationId, boolean automatic);

    // Revertir acciones realizadas en la base de datos
    void revertAcceptInvitation(UUID operationId);
}
