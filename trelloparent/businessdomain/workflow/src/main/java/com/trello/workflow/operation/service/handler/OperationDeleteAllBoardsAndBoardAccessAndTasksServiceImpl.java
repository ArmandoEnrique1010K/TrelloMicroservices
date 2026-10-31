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
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // RESULTADO CUANDO NO HAY TABLEROS: []
        // List<UUID> boardsIds = currentOperation.getBoardIds();

        // Logica de negocio: deshabilitar el estado de los tableros incluyendo sus
        // boardsAccess
        List<UUID> normalizedBoardsIds = currentOperation.getBoardIds() == null
                ? List.of()
                : currentOperation.getBoardIds();

        if (!normalizedBoardsIds.isEmpty()) {
            boardWorkflowService
                    .disableAllBoardsStatusByIdAndDisableAllBoardAccessesStatus(
                            normalizedBoardsIds);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteAllBoardsAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException {

        Operation operation = operationWorkflowService
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DELETE_ALL_BOARDS_AND_BOARD_ACCESS_AND_TASKS) {
            throw new OperationConflictException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        List<UUID> normalizedBoardsIds = operation.getBoardIds() == null
                ? List.of()
                : operation.getBoardIds();

        // Restaurar tableros y permisos solamente si existen.
        if (!normalizedBoardsIds.isEmpty()) {
            boardWorkflowService
                    .enableAllBoardsStatusByIdAndEnableAllBoardAccessesStatus(
                            normalizedBoardsIds);
        }

        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
