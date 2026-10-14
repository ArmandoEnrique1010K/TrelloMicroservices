package com.trello.workflow.operation.service.procesors;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.operation.service.OperationAddBoardAccessService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationProcessorImpl implements OperationProcessor {

    private final OperationWorkflowService operationWorkflowService;
    private final OperationAddBoardAccessService operationAddBoardAccessService;

    public OperationProcessorImpl(
            OperationWorkflowService operationWorkflowService,
            OperationAddBoardAccessService operationAddBoardAccessService) {

        this.operationWorkflowService = operationWorkflowService;
        this.operationAddBoardAccessService = operationAddBoardAccessService;
    }

    // Ejecuta la operación original
    @Override
    public void processOperation(UUID operationId) {
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // TODO: AQUI ESTABA EL PROBLEMA DE LA EXCEPCIÓN
        // SE VEIA QUE EN LA UI DE SWAGGER SIEMPRE LANZABA UN BusinessRuleException Y
        // ERA PORQUE EN EL MICROSERVICIO PROJECT SE HABIA DEFINIDO UNA CLASE
        // WorkflowErrorDecoder QUE PROVOCABA QUE CUALQUIER EXCEPCIÓN NO DEFINIDA
        // SIEMPRE CAIA EN UN BusinessRuleException

        // Solamente ejecutar operaciones con estado PENDING O CREATED
        if (operation.getStatus() != OperationStatus.CREATED && operation.getStatus() != OperationStatus.PENDING) {
            // TODO: CORREGIR LA EXCEPCIÓN
            throw new OperationNotPendingException();
        }

        // Evaluar por cada tipo de operación
        switch (operation.getType()) {
            case ADD_BOARD_ACCESS ->
                operationAddBoardAccessService.applyAddBoardAccess(operation);

            // case ACTIVATE_BOARD_ACCESS ->

            // NOTA: ESTO NUNCA PASARA
            default -> throw new IllegalArgumentException(
                    "Unsupported operation type: " + operation.getType());
        }
    }
}
