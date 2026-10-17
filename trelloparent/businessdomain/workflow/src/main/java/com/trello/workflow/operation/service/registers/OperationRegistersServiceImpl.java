package com.trello.workflow.operation.service.registers;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationRegistersServiceImpl implements OperationRegistersService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public OperationRegistersServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    // Registra la operación antes de ejecutar el efecto
    // El operationId debe ser el recibido mediante X-Operation-Id
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAddBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId) {
        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        if (existingOperation.isPresent()) {

            Operation existing = existingOperation.get();

            // Impedir reutilizar el mismo identificador para una operación diferente.
            if (existing.getType() != OperationType.ADD_BOARD_ACCESS
                    || !existing.getBoardId().equals(boardId)
                    || !existing.getUserId().equals(userId)
                    || existing.getNewRole() != newRole) {

                // Violación de reglas de negocio
                throw new BusinessRuleException();
            }

            // Un reintento de una operación ya completada no debe volver a ejecutar el
            // alta.
            switch (existing.getStatus()) {
                case PENDING -> {
                    return existing;
                }

                // Estos casos representan que la operacion no esta pendiente
                case CREATED, PROCESSING, COMPLETED, MANUAL_REVIEW ->
                    throw new OperationNotPendingException();
            }
        }

        // Validar reglas de negocio
        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        // Crear una nueva operación
        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ADD_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setOldUserActive(null);
        operation.setOldRole(null);
        operation.setNewRole(newRole);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        // Retornar la entidad completa de la operación
        return operation;
    }

    @Override
    public Operation registerActivateBoardAccess(UUID boardId, UUID userId, Role newRole,
            UUID operationId) {
        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            // Impedir reutilizar el mismo identificador para una operación diferente.
            if (existing.getType() != OperationType.ACTIVATE_BOARD_ACCESS
                    || !existing.getBoardId().equals(boardId)
                    || !existing.getUserId().equals(userId)
                    || existing.getNewRole() != newRole) {

                // Violación de reglas de negocio
                throw new BusinessRuleException();
            }

            // Un reintento de una operación ya completada no debe volver a ejecutar el
            // alta.
            switch (existing.getStatus()) {
                case PENDING -> {
                    return existing;
                }

                // Estos casos representan que la operacion no esta pendiente
                case CREATED, PROCESSING, COMPLETED, MANUAL_REVIEW ->
                    throw new OperationNotPendingException();
            }
        }

        // Validar reglas de negocio
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        // Crear una nueva operación
        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACTIVATE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);

        // Si o si va a tener un valor false
        operation.setOldUserActive(findedBoardAccess.isUserActive());
        // Toma el rol antiguo y el nuevo
        operation.setOldRole(findedBoardAccess.getRole());
        operation.setNewRole(newRole);

        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        // Retornar la entidad completa de la operación
        return operation;
    }
}
