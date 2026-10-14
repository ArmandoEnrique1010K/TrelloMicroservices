package com.trello.workflow.services;

import java.util.Optional;
import java.util.UUID;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationType;

public interface OperationWorkflowService {
    Optional<Operation> findOperationById(UUID operationId);

    Optional<Operation> findOperationByBoardIdAndUserIdAndOperationType(UUID boardId, UUID userId,
            OperationType operationType);

    Operation saveOperation(Operation operation);
}
