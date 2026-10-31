package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;

public interface OperationDeleteBoardAndBoardAccessAndTasksService {
    void applyDeleteBoardAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException;

    void revertDeleteBoardAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException;
}
