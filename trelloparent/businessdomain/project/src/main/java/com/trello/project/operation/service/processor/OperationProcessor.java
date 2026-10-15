package com.trello.project.operation.service.processor;

import java.util.UUID;

public interface OperationProcessor {
    // Representa un procesador global de operaciones
    void processOperation(UUID operationId);
}
