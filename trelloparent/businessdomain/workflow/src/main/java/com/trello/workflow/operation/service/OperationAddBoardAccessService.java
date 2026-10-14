package com.trello.workflow.operation.service;

import java.util.UUID;

import com.trello.workflow.entities.Operation;

public interface OperationAddBoardAccessService {
    // Servicios atomicos por una operación que requiere comunicación entre
    // microservicios

    // Aplicar logica de negocio
    void applyAddBoardAccess(Operation operation);

    // Revertir logica de negocio
    void revertAddBoardAccess(UUID operationId);
}
