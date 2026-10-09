package com.trello.workflow.operation.service;

import java.util.UUID;

public interface OperationService {
    void compensateOperation(UUID operationId);
}
