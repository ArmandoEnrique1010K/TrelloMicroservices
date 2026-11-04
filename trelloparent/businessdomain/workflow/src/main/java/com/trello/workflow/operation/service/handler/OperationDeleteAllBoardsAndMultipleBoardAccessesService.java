package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;

public interface OperationDeleteAllBoardsAndMultipleBoardAccessesService {
    void applyDeleteAllBoardsAndMultipleBoardAccesses(UUID operationId)
            throws OperationNotFoundException;

    void revertDeleteAllBoardsAndMultipleBoardAccesses(UUID operationId)
            throws OperationNotFoundException, OperationConflictException;
}
