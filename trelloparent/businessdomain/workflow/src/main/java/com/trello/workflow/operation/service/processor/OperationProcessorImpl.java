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
import com.trello.workflow.operation.service.handler.OperationChangeRoleBoardAccessService;
import com.trello.workflow.operation.service.handler.OperationDeactivateBoardAccessService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

// Servicio para procesar la operación
@Slf4j
@Service
public class OperationProcessorImpl implements OperationProcessor {

    private final OperationWorkflowService operationWorkflowService;
    private final OperationAddBoardAccessService operationAddBoardAccessService;
    private final OperationActivateBoardAccessService operationActivateBoardAccessService;
    private final OperationChangeRoleBoardAccessService operationChangeRoleBoardAccessService;
    private final OperationDeactivateBoardAccessService operationDeactivateBoardAccessService;

    public OperationProcessorImpl(
            OperationWorkflowService operationWorkflowService,
            OperationAddBoardAccessService operationAddBoardAccessService,
            OperationActivateBoardAccessService operationActivateBoardAccessService,
            OperationChangeRoleBoardAccessService operationChangeRoleBoardAccessService,
            OperationDeactivateBoardAccessService operationDeactivateBoardAccessService) {

        this.operationWorkflowService = operationWorkflowService;
        this.operationAddBoardAccessService = operationAddBoardAccessService;
        this.operationActivateBoardAccessService = operationActivateBoardAccessService;
        this.operationChangeRoleBoardAccessService = operationChangeRoleBoardAccessService;
        this.operationDeactivateBoardAccessService = operationDeactivateBoardAccessService;
    }

    @Override
    public void processOperation(UUID operationId) throws OperationNotPendingException, BusinessRuleException {
        // 1° Busca la operación y valida su estado
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // Solamente ejecutar operaciones con estado CREATED O PENDING
        if (operation.getStatus() != OperationStatus.CREATED && operation.getStatus() != OperationStatus.PENDING) {
            throw new OperationNotPendingException();
        }

        // 2° Evaluar por cada tipo de operación
        switch (operation.getType()) {
            case ADD_BOARD_ACCESS ->
                operationAddBoardAccessService.applyAddBoardAccess(operation.getId());

            case ACTIVATE_BOARD_ACCESS ->
                operationActivateBoardAccessService.applyActivateBoardAccess(operation.getId());

            case CHANGE_ROLE_BOARD_ACCESS ->
                operationChangeRoleBoardAccessService.applyChangeRoleBoardAccess(operation.getId());

            case DEACTIVATE_BOARD_ACCESS ->
                operationDeactivateBoardAccessService.applyDeactivateBoardAccess(operation.getId());

            // Esto nunca pasara, si el caso no coincide con uno de los valores del enum
            // OperationType
            default -> throw new BusinessRuleException();
        }
    }
}
