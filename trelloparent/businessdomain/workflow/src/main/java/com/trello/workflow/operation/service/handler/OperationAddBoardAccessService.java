package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;

public interface OperationAddBoardAccessService {
    // Servicios atomicos por una operación que requiere comunicación entre
    // microservicios

    // Aplicar logica de negocio
    void applyAddBoardAccess(UUID operationId)
            throws OperationNotFoundException, BoardAccessAlreadyExistsException;

    // Revertir logica de negocio
    void revertAddBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException;
}
