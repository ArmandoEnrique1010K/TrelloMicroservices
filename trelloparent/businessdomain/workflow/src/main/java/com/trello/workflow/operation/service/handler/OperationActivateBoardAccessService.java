package com.trello.workflow.operation.service.handler;

import java.util.UUID;

import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;

public interface OperationActivateBoardAccessService {
    void applyActivateBoardAccess(UUID operationId)
            throws OperationNotFoundException, BoardAccessUserActiveNotAllowedException;

    void revertActivateBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException;
}
