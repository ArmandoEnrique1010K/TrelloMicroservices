package com.trello.workflow.operation.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.exception.OperationCannotBeCompensatedException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationServiceImpl implements OperationService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public OperationServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void compensateOperation(UUID operationId) {
        // 1. Buscar la operación.
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        // 2. Verificar su estado y tipo.

        // La operación ya fue compensada.
        // No se debe compensar dos veces.
        if (operation.getStatus() == OperationStatus.COMPENSATED) {
            return;
        }
        // Solo se compensan operaciones completadas.
        if (operation.getStatus() != OperationStatus.COMPLETED) {
            throw new OperationCannotBeCompensatedException();
        }

        // 3. Revertir el cambio de BoardAccess.
        switch (operation.getType()) {

            case OperationType.ADD_BOARD_ACCESS ->
                compensateAddBoardAccess(operation);

            case OperationType.ACTIVATE_BOARD_ACCESS ->
                compensateActivateBoardAccess(operation);
        }

        // 4. Marcar la operación como COMPENSATED.
        // Se actualiza el estado únicamente después
        // de revertir correctamente los cambios de negocio.
        operation.setStatus(OperationStatus.COMPENSATED);

        // 5. Confirmar ambas modificaciones juntas.
        operationWorkflowService.saveOperation(operation);
    };

    private void compensateAddBoardAccess(Operation operation) {
        // Revertir el acceso creado por la operación.
        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();

        boardAccessWorkflowService.deleteBoardAccess(boardId, userId);
    };

    private void compensateActivateBoardAccess(Operation operation) {
        // Restaurar el estado y el rol anteriores del acceso.
        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        Role previousRole = operation.getPreviousRole();

        boardAccessWorkflowService.revertActivateBoardAccess(boardId, userId, previousRole);
    };
}
