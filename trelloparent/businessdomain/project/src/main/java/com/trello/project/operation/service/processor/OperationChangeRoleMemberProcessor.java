package com.trello.project.operation.service.processor;

import java.util.UUID;

import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationChangeRoleMemberProcessor {
    MemberResponse processChangeRoleMemberOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, MemberNotFoundException,
            OperationNotPendingException, OperationManualReviewException, MemberInactiveException,
            OperationCompensatedException, BusinessRuleException;
}
