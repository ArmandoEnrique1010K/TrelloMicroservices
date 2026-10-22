package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationDeleteAllBoardsAndBoardAccessAndTasksServiceImpl
        implements OperationDeleteAllBoardsAndBoardAccessAndTasksService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationDeleteAllBoardsAndBoardAccessAndTasksServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteAllBoardsAndBoardAccessAndTasks(UUID operationId) throws OperationNotFoundException {
        Operation currentOperation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        currentOperation.setStatus(OperationStatus.PROCESSING);
        currentOperation.setAttempts(currentOperation.getAttempts() + 1);
        operationWorkflowService.saveOperation(currentOperation);

        List<UUID> boardsIds = currentOperation.getBoardIds();

        // Logica de negocio: deshabilitar el estado de los tableros incluyendo sus
        // boardsAccess
        boardWorkflowService.disableAllBoardsStatusByIdAndDisableAllBoardAccessesStatus(boardsIds);

        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteAllBoardsAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        if (operation.getType() != OperationType.DELETE_ALL_BOARDS_AND_BOARD_ACCESS_AND_TASKS) {
            throw new OperationConflictException();
        }

        if (operation.getStatus() == OperationStatus.CREATED
                && operation.getStatus() == OperationStatus.COMPLETED
                && operation.getStatus() == OperationStatus.MANUAL_REVIEW) {
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        List<UUID> boardsIds = operation.getBoardIds();

        // Habilitar estado de los tableros incluyendo sus permisos de acceso
        boardWorkflowService.enableAllBoardsStatusByIdAndEnableAllBoardAccessesStatus(boardsIds);

        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
