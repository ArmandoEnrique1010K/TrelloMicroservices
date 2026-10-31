package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;

public interface OperationDeleteAllBoardsAndBoardAccessAndTasksService {
    void applyDeleteAllBoardsAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException;

    void revertDeleteAllBoardsAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException;
}
