package com.trello.workflow.operation.service.processor;

import java.util.UUID;

import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;

public interface OperationProcessor {
    // void processOperation(UUID operationId)
    // throws OperationNotPendingException, BusinessRuleException;

    void processAddBoardAccessOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;

    void processActivateBoardAccessOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;

    void processChangeRoleBoardAccessOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;

    void processDeactivateBoardAccessOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;

    void processAddBoardAndBoardAccessUserOwnerOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;

    void processDeleteBoardAndBoardAccessesOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;

    void processDeleteAllBoardsAndMultipleBoardAccessesOperation(UUID operationId)
            throws OperationNotFoundException, OperationNotPendingException, OperationConflictException;
}
