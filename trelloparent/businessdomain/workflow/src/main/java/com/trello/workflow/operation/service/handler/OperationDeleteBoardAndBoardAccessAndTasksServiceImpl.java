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
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeleteBoardAndBoardAccessAndTasksServiceImpl
        implements OperationDeleteBoardAndBoardAccessAndTasksService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationDeleteBoardAndBoardAccessAndTasksServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    // TODO: PROGRAMAR UN SCHEDULER PARA BORRAR TABLEROS FISICAMENTE QUE TENGAN EL
    // ESTADO FALSE
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyDeleteBoardAndBoardAccessAndTasks(UUID operationId) throws OperationNotFoundException {
        Operation currentOperation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        UUID boardId = currentOperation.getBoardId();

        // Logica de negocio

        // Aunque de esta manera garantiza que se ha eliminado el tablero, no garantiza
        // que se pueda recuperar en caso de que se quiera revertir la operación
        // boardWorkflowService.deleteBoardById(boardId);

        // Para aquello se opta por cmabiar el estado del tablero

        // Deshabilitar estado del tablero
        // boardWorkflowService.disableBoardStatusById(boardId);

        // Deshabilitar cada uno de los estados de los permiso de acceso
        // boardAccessWorkflowService.disableAllBoardAccessStatusByBoardId(boardId);

        // Realizar ambas operaciones en un solo metodo del servicio, conlleva a que se
        // realice 2 queries de eliminación
        boardWorkflowService.disableBoardStatusByIdAndDisableAllBoardAccessStatus(boardId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertDeleteBoardAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {

        log.info("Buscando operacion");
        log.info("ID de operacion:" + operationId);

        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS) {
            log.info("Operacion en conflicto");
            throw new OperationConflictException();
        }

        if (operation.getStatus() == OperationStatus.CREATED
                && operation.getStatus() == OperationStatus.COMPLETED) {
            log.info("Operacion no pendiente");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        UUID boardId = operation.getBoardId();

        // Habilitar estado del tablero
        // boardWorkflowService.enableBoardStatusById(boardId);

        // Habilitar cada uno de los estados de los permiso de acceso
        // boardAccessWorkflowService.enableAllBoardAccessStatusByBoardId(boardId);

        // Realizar ambas operaciones en 1 solo query

        log.info("Habilitando permisos de acceso y tablero");
        boardWorkflowService.enableBoardStatusByIdAndEnableAllBoardAccessStatus(boardId);
        log.info("Operacion pendiente");
        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
