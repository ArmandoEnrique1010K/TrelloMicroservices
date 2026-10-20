package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.operation.exception.OperationNotFoundException;

public interface OperationAddBoardAndBoardAccessUserOwnerService {
    void applyAddBoardAndBoardAccessUserOwner(UUID operationId)
            throws BoardAlreadyExistsException, OperationNotFoundException;

    void revertAddBoardAndBoardAccessUserOwner(UUID operationId);
}
