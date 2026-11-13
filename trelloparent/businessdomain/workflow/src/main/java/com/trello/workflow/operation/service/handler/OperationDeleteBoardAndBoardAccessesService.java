package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.exception.BoardNotFoundException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;

public interface OperationDeleteBoardAndBoardAccessesService {
    void applyDeleteBoardAndBoardAccesses(UUID operationId)
            throws OperationNotFoundException, BoardNotFoundException;

    void revertDeleteBoardAndBoardAccesses(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, BoardNotFoundException;

    void cancelAllOperationsByBoardId(UUID boardId);
}
