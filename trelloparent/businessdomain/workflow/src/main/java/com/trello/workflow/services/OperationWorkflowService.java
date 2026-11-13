package com.trello.workflow.services;

import java.util.Optional;
import java.util.UUID;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;

public interface OperationWorkflowService {
    Optional<Operation> findOptionalOperationById(UUID operationId);

    Operation saveOperation(Operation operation);

    void changeStatusOperationsByBoardId(UUID boardId, OperationStatus status);
}
