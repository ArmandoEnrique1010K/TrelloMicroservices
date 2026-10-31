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
import com.trello.workflow.exception.BoardAccessNotFoundException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationDeactivateBoardAccessServiceImpl implements OperationDeactivateBoardAccessService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public OperationDeactivateBoardAccessServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeactivateBoardAccess(UUID operationId)
            throws OperationNotFoundException, BoardAccessUserActiveNotAllowedException, BoardAccessNotFoundException {

        Operation currentOperation = operationWorkflowService
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();

        BoardAccess findedBoardAccess = boardAccessWorkflowService
                .findBoardAccessByBoardIdAndUserIdAndUserActiveAndStatusTrue(
                        boardId,
                        userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        findedBoardAccess.setUserActive(false);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeactivateBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, BoardAccessNotFoundException {

        Operation operation = operationWorkflowService
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DEACTIVATE_BOARD_ACCESS) {
            throw new OperationConflictException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        Role oldRole = operation.getOldRole();

        boardAccessWorkflowService.activateBoardAccessAndChangeRoleByBoardIdAndUserIdAndStatusTrue(
                boardId, userId, oldRole);

        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

}
