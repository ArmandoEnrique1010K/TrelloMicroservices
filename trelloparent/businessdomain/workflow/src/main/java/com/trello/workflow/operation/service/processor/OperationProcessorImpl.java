package com.trello.workflow.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.operation.service.handler.OperationActivateBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationAddBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationAddBoardAndBoardAccessUserOwnerService;
import com.trello.workflow.operation.service.handler.OperationChangeRoleBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationDeactivateBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationDeleteAllBoardsAndBoardAccessAndTasksService;
import com.trello.workflow.operation.service.handler.OperationDeleteBoardAndBoardAccessAndTasksService;
import com.trello.workflow.services.OperationWorkflowService;

// Servicio para procesar la operación
@Service
public class OperationProcessorImpl implements OperationProcessor {

    private final OperationWorkflowService operationWorkflowService;
    private final OperationAddBoardAccessService operationAddBoardAccessService;
    private final OperationActivateBoardAccessService operationActivateBoardAccessService;
    private final OperationChangeRoleBoardAccessService operationChangeRoleBoardAccessService;
    private final OperationDeactivateBoardAccessService operationDeactivateBoardAccessService;
    private final OperationAddBoardAndBoardAccessUserOwnerService operationAddBoardAndBoardAccessUserOwnerService;
    private final OperationDeleteBoardAndBoardAccessAndTasksService operationDeleteBoardAndBoardAccessAndTasksService;
    private final OperationDeleteAllBoardsAndBoardAccessAndTasksService operationDeleteAllBoardsAndBoardAccessAndTasksService;

    public OperationProcessorImpl(
            OperationWorkflowService operationWorkflowService,
            OperationAddBoardAccessService operationAddBoardAccessService,
            OperationActivateBoardAccessService operationActivateBoardAccessService,
            OperationChangeRoleBoardAccessService operationChangeRoleBoardAccessService,
            OperationDeactivateBoardAccessService operationDeactivateBoardAccessService,
            OperationAddBoardAndBoardAccessUserOwnerService operationAddBoardAndBoardAccessUserOwnerService,
            OperationDeleteBoardAndBoardAccessAndTasksService operationDeleteBoardAndBoardAccessAndTasksService,
            OperationDeleteAllBoardsAndBoardAccessAndTasksService operationDeleteAllBoardsAndBoardAccessAndTasksService) {
        this.operationWorkflowService = operationWorkflowService;
        this.operationAddBoardAccessService = operationAddBoardAccessService;
        this.operationActivateBoardAccessService = operationActivateBoardAccessService;
        this.operationChangeRoleBoardAccessService = operationChangeRoleBoardAccessService;
        this.operationDeactivateBoardAccessService = operationDeactivateBoardAccessService;
        this.operationAddBoardAndBoardAccessUserOwnerService = operationAddBoardAndBoardAccessUserOwnerService;
        this.operationDeleteBoardAndBoardAccessAndTasksService = operationDeleteBoardAndBoardAccessAndTasksService;
        this.operationDeleteAllBoardsAndBoardAccessAndTasksService = operationDeleteAllBoardsAndBoardAccessAndTasksService;
    }

    // Procesa una operación registrada según su tipo y estado actual.
    @Override
    public void processOperation(UUID operationId)
            throws OperationNotPendingException, BusinessRuleException {
        // Recuperar la operación que se va a procesar.
        Operation operation = operationWorkflowService
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // Permitir el procesamiento únicamente de operaciones nuevas o pendientes.
        if (operation.getStatus() != OperationStatus.CREATED && operation.getStatus() != OperationStatus.PENDING) {
            throw new OperationNotPendingException();
        }

        // Delegar la ejecución al servicio correspondiente al tipo de operación.
        switch (operation.getType()) {
            case ADD_BOARD_ACCESS ->
                operationAddBoardAccessService.applyAddBoardAccess(operation.getId());

            case ACTIVATE_BOARD_ACCESS ->
                operationActivateBoardAccessService.applyActivateBoardAccess(operation.getId());

            case CHANGE_ROLE_BOARD_ACCESS ->
                operationChangeRoleBoardAccessService.applyChangeRoleBoardAccess(operation.getId());

            case DEACTIVATE_BOARD_ACCESS ->
                operationDeactivateBoardAccessService.applyDeactivateBoardAccess(operation.getId());

            case ADD_BOARD_AND_BOARD_ACCESS_USER_OWNER ->
                operationAddBoardAndBoardAccessUserOwnerService
                        .applyAddBoardAndBoardAccessUserOwner(operation.getId());

            case DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS ->
                operationDeleteBoardAndBoardAccessAndTasksService
                        .applyDeleteBoardAndBoardAccessAndTasks(operation.getId());

            case DELETE_ALL_BOARDS_AND_BOARD_ACCESS_AND_TASKS ->
                operationDeleteAllBoardsAndBoardAccessAndTasksService
                        .applyDeleteAllBoardsAndBoardAccessAndTasks(operation.getId());

            // Rechazar los tipos de operación que no tienen un procesador definido.
            default -> throw new BusinessRuleException();
        }
    }
}
