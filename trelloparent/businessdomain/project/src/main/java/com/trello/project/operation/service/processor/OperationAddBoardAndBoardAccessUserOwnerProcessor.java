package com.trello.project.operation.service.processor;

import java.util.UUID;

import com.trello.project.board.dto.response.BoardResponse;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationNotFoundException;

public interface OperationAddBoardAndBoardAccessUserOwnerProcessor {
    BoardResponse processAddBoardAndBoardAccessUserOwnerOperation(UUID operationId) throws OperationNotFoundException,
            OperationConflictException;

}
