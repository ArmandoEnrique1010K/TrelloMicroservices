package com.trello.workflow.operation.service;

import java.util.UUID;

import com.trello.workflow.entities.Operation;

public interface OperationActivateBoardAccessService {
    void applyActivateBoardAccess(Operation operation);

    void revertActivateBoardAccess(UUID operationId);
}
