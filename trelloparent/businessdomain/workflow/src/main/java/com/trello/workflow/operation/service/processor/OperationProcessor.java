package com.trello.workflow.operation.service.processor;

import java.util.UUID;

import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationNotPendingException;

public interface OperationProcessor {
    void processOperation(UUID operationId)
            throws OperationNotPendingException, BusinessRuleException;
}
