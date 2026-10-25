package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationChangeRoleBoardAccessServiceImpl implements OperationChangeRoleBoardAccessService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public OperationChangeRoleBoardAccessServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyChangeRoleBoardAccess(UUID operationId)
            throws OperationNotFoundException, BoardAccessUserActiveNotAllowedException {

        Operation currentOperation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();
        Role newRole = currentOperation.getNewRole();

        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        findedBoardAccess.setRole(newRole);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertChangeRoleBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {

        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.CHANGE_ROLE_BOARD_ACCESS) {
            throw new OperationConflictException();
        }

        if (operation.getStatus() == OperationStatus.CREATED
                && operation.getStatus() == OperationStatus.COMPLETED) {
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        Role oldRole = operation.getOldRole();

        boardAccessWorkflowService.changeBoardAccessByBoardIdAndUserId(
                boardId, userId, oldRole);

        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
