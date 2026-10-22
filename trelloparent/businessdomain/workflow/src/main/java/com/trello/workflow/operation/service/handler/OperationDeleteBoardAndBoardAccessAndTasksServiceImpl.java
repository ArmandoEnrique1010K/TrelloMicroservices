package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationDeleteBoardAndBoardAccessAndTasksServiceImpl
        implements OperationDeleteBoardAndBoardAccessAndTasksService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationDeleteBoardAndBoardAccessAndTasksServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService, BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteBoardAndBoardAccessAndTasks(UUID operationId) throws OperationNotFoundException {
        Operation currentOperation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        currentOperation.setStatus(OperationStatus.PROCESSING);
        currentOperation.setAttempts(currentOperation.getAttempts() + 1);
        operationWorkflowService.saveOperation(currentOperation);

        UUID boardId = currentOperation.getBoardId();

        // Logica de negocio

        // Aunque de esta manera garantiza que se ha eliminado el tablero, no garantiza
        // que se pueda recuperar en caso de que se quiera revertir la operación
        // boardWorkflowService.deleteBoardById(boardId);

        // IDEA: LOS TABLEROS DEBEN TENER UN ESTADO BOOLEANO PARA RESTRINGIR EL ACCESO
        // AL TABLERO (SE CONSIDERA QUE FUE ELIMINADO LOGICAMENTE)

        // Deshabilitar estado del tablero
        // boardWorkflowService.disableBoardStatusById(boardId);

        // Deshabilitar cada uno de los estados de los permiso de acceso
        // boardAccessWorkflowService.disableAllBoardAccessStatusByBoardId(boardId);

        // Realizar ambas operaciones en un solo metodo del servicio, conlleva a que se
        // realice 2 queries de eliminación
        boardWorkflowService.disableBoardStatusByIdAndDisableAllBoardAccessStatus(boardId);
        // TODO: PROGRAMAR UN SCHEDULER PARA BORRAR TABLEROS FISICAMENTE QUE TENGAN EL
        // ESTADO FALSE

        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteBoardAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        if (operation.getType() != OperationType.DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS) {
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

        UUID boardId = operation.getBoardId();

        // Habilitar estado del tablero
        boardWorkflowService.enableBoardStatusById(boardId);

        // Habilitar cada uno de los estados de los permiso de acceso
        boardAccessWorkflowService.enableAllBoardAccessStatusByBoardId(boardId);

        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

}
