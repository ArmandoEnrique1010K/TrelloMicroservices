package com.trello.project.operation.service.handler;

import java.util.UUID;

import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationChangeRoleMemberService {
    void applyChangeRoleMember(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, MemberNotFoundException,
            MemberInactiveException;

    void revertChangeRoleMember(UUID operationId)
            throws OperationNotFoundException, MemberNotFoundException, MemberInactiveException;
}
