package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
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
        boardWorkflowService.deleteBoardById(boardId);

        // TODO:
        // IDEA: LOS TABLEROS DEBEN TENER UN ESTADO BOOLEANO PARA RESTRINGIR EL ACCESO
        // AL TABLERO (SE CONSIDERA QUE FUE ELIMINADO LOGICAMENTE)

        // SE PROGRAMARA UN SCHEDULER PARA BORRAR TABLEROS FISICAMENTE

        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Override
    public void revertDeleteBoardAndBoardAccessAndTasks(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revertDeleteBoardAndBoardAccessAndTasks'");
    }

}
