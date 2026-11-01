package com.trello.project.operation.service.handler;

import java.util.UUID;

import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;

public interface OperationAddBoardAndBoardAccessUserOwnerService {
    void applyAddBoardAndBoardAccessUserOwner(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, BoardAlreadyExistsException,
            WorkspaceNotFoundException;

    void revertAddBoardAndBoardAccessUserOwner(UUID operationId)
            throws OperationNotFoundException, WorkspaceNotFoundException, BoardNotFoundException;
}
