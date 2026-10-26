package com.trello.project.operation.service.processor;

import java.util.UUID;

import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationNotFoundException;

public interface OperationChangeRoleMemberProcessor {
    MemberResponse processChangeRoleMemberOperation(UUID operationId) throws OperationNotFoundException,
            OperationConflictException;

}
