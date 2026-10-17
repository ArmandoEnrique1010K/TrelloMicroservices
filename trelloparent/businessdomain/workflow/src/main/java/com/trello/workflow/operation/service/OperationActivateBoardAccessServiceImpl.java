package com.trello.workflow.operation.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationActivateBoardAccessServiceImpl implements OperationActivateBoardAccessService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public OperationActivateBoardAccessServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    // TODO: HAY UN PROBLEMA, NO SE PUEDE GUARDAR UNA OPERACION DE ACTIVAR INVITADO
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyActivateBoardAccess(Operation operation) {
        Operation currentOperation = operationWorkflowService
                .findOperationById(operation.getId())
                .orElseThrow(OperationNotFoundException::new);

        // Solamente ejecutar operaciones con estado PENDING
        if (currentOperation.getStatus() != OperationStatus.PENDING) {
            throw new OperationNotPendingException();
        }

        // Aumenta el numero de intentos en 1
        currentOperation.setStatus(OperationStatus.PROCESSING);
        currentOperation.setAttempts(currentOperation.getAttempts() + 1);
        operationWorkflowService.saveOperation(currentOperation);

        // Toma los datos de la operación
        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();

        // Rol nuevo y usuario activo previo
        Role newRole = currentOperation.getNewRole();
        Boolean userActive = currentOperation.getOldUserActive();

        // Volver a preguntar por reglas de negocio
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        // Actualizar el permiso de acceso
        findedBoardAccess.setRole(newRole);

        // Valor opuesto
        findedBoardAccess.setUserActive(!userActive);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);

        // Marcar operación como completada
        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertActivateBoardAccess(UUID operationId) {
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(IllegalStateException::new);

        if (operation.getType() != OperationType.ACTIVATE_BOARD_ACCESS) {
            throw new BusinessRuleException();
        }

        if (operation.getStatus() != OperationStatus.PENDING &&
                operation.getStatus() != OperationStatus.PROCESSING) {
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        try {
            boardAccessWorkflowService.inactivateBoardAccessAndChangeRoleByBoardIdAndUserId(operation.getBoardId(),
                    operation.getUserId(), operation.getOldRole());
        } catch (RuntimeException exception) {
            operation.setStatus(OperationStatus.MANUAL_REVIEW);
            operation.setCompletedAt(null);
            operationWorkflowService.saveOperation(operation);

        } catch (Exception ex) {
            throw ex;
        }

    }

}
