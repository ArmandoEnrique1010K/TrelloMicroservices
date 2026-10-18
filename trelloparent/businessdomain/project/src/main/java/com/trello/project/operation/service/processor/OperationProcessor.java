package com.trello.project.operation.service.processor;

import java.util.UUID;

import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationProcessor {
    // Representa un procesador global de operaciones
    void processOperation(UUID operationId) throws OperationNotFoundException, OperationNotPendingException;
}
