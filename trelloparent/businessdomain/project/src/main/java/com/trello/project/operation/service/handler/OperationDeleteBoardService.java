package com.trello.project.operation.service.handler;

import java.util.UUID;

import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationDeleteBoardService {
    void applyDeleteBoard(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException;

    void revertDeleteBoard(UUID operationId)
            throws OperationNotFoundException;

}
