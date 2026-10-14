package com.trello.workflow.operation.service.procesors;

import java.util.UUID;

public interface OperationProcessor {
    void processOperation(UUID operationId);
}
