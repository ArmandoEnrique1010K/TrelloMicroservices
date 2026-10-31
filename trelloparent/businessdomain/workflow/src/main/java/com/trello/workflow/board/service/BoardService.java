package com.trello.workflow.board.service;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.exception.BoardNotFoundException;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.ManyBoardsNotFoundException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;

public interface BoardService {
    void addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            BoardAlreadyExistsException, OperationNotFoundException;

    void deleteBoardAndBoardAccessAndTasks(UUID boardId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            BoardNotFoundException, OperationNotFoundException;

    void deleteAllBoardsByIdsAndBoardAccessAndTasks(List<UUID> boardsIds, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            ManyBoardsNotFoundException, OperationNotFoundException;
}
