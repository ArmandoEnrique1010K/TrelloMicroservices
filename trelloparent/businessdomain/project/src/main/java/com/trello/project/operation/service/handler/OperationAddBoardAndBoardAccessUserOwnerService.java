package com.trello.project.operation.service.handler;

import java.util.UUID;

import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationAddBoardAndBoardAccessUserOwnerService {
    void applyAddBoardAndBoardAccessUserOwner(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, InvitationConfirmedException;

    void revertAddBoardAndBoardAccessUserOwner(UUID operationId)
            throws OperationNotFoundException;
}
