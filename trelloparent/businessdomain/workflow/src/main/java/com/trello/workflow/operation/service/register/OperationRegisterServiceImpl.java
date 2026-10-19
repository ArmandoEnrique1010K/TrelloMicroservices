package com.trello.workflow.operation.service.register;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

@Service
public class OperationRegisterServiceImpl implements OperationRegisterService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public OperationRegisterServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    // Registra la operación antes de ejecutar el efecto
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAddBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessAlreadyExistsException {
        // 1° Busca una operación existente en la base de datos
        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            // Impedir reutilizar el mismo identificador para una operación diferente.
            if (existing.getType() != OperationType.ADD_BOARD_ACCESS
                    || !existing.getBoardId().equals(boardId)
                    || !existing.getUserId().equals(userId)
                    || existing.getNewRole() != newRole) {

                // Excepción de una operación conflictiva
                throw new OperationConflictException();
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

        // 2° En el caso de que no exista la operación se procede a crearla

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerActivateBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {
        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            // Impedir reutilizar el mismo identificador para una operación diferente.
            if (existing.getType() != OperationType.ACTIVATE_BOARD_ACCESS
                    || !existing.getBoardId().equals(boardId)
                    || !existing.getUserId().equals(userId)
                    || existing.getNewRole() != newRole) {

                // Violación de reglas de negocio
                throw new OperationConflictException();
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
            throw new BoardAccessUserActiveNotAllowedException();
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

    @Override
    public Operation registerChangeRoleBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {

        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();
            if (existing.getType() != OperationType.CHANGE_ROLE_BOARD_ACCESS
                    || !existing.getBoardId().equals(boardId)
                    || !existing.getUserId().equals(userId)
                    || existing.getNewRole() != newRole) {
                throw new OperationConflictException();
            }

            switch (existing.getStatus()) {
                case PENDING -> {
                    return existing;
                }

                case CREATED, PROCESSING, COMPLETED, MANUAL_REVIEW ->
                    throw new OperationNotPendingException();
            }
        }

        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.CHANGE_ROLE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);

        operation.setOldRole(findedBoardAccess.getRole());
        operation.setNewRole(newRole);

        operation.setAttempts(0);
        operationWorkflowService.saveOperation(operation);

        return operation;
    }
}
