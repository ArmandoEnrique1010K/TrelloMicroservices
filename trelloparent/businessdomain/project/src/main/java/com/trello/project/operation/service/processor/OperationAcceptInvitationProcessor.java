package com.trello.project.operation.service.processor;

import java.util.UUID;

import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.InvitationNotFoundException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationAcceptInvitationProcessor {
    void processAcceptInvitationOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, InvitationNotFoundException, OperationNotPendingException,
            OperationManualReviewException;
}
