package com.trello.project.operation.service;

import java.util.UUID;

public interface OperationProcessor {
    void processOperation(UUID operationId);
}
