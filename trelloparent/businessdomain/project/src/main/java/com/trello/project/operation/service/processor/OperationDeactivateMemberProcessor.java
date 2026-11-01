package com.trello.project.operation.service.processor;

import java.util.UUID;

import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationDeactivateMemberProcessor {
    void processDeactivateMemberOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, MemberNotFoundException, OperationNotPendingException,
            OperationManualReviewException;
}
