package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.exception.OperationConflictException;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyActivateBoardAccess(UUID operationId)
            throws OperationNotFoundException, BoardAccessUserActiveNotAllowedException {
        // 1° Buscar operación
        Operation currentOperation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // 2° Aumentar el numero de intentos en 1
        currentOperation.setStatus(OperationStatus.PROCESSING);
        currentOperation.setAttempts(currentOperation.getAttempts() + 1);
        operationWorkflowService.saveOperation(currentOperation);

        // Toma los datos de la operación
        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();

        // Rol nuevo y usuario activo previo
        Role newRole = currentOperation.getNewRole();
        Boolean userActive = currentOperation.getOldUserActive();

        // 3° Volver a preguntar si existe tablero y si el usuario no tiene el rol de
        // OWNER
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        // 4° Actualizar el permiso de acceso
        findedBoardAccess.setRole(newRole);

        // Valor opuesto de usuario activo
        findedBoardAccess.setUserActive(!userActive);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);

        // 5° Marcar operación como completada
        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertActivateBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {
        // 1° Buscar operación
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // 2° Verificar el tipo de operación
        if (operation.getType() != OperationType.ACTIVATE_BOARD_ACCESS) {
            throw new OperationConflictException();
        }

        // Solo se puede compensar una operación cuyo estado sea:
        // Permitido: PROCESSING, PENDING
        if (operation.getStatus() == OperationStatus.CREATED
                && operation.getStatus() == OperationStatus.COMPLETED
                && operation.getStatus() == OperationStatus.MANUAL_REVIEW) {
            throw new OperationNotPendingException();
        }

        // 3° Actualizar estado de la operación
        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        // try {
        // boardAccessWorkflowService.inactivateBoardAccessAndChangeRoleByBoardIdAndUserId(operation.getBoardId(),
        // operation.getUserId(), operation.getOldRole());
        // } catch (RuntimeException exception) {
        // operation.setStatus(OperationStatus.MANUAL_REVIEW);
        // operation.setCompletedAt(null);
        // operationWorkflowService.saveOperation(operation);
        // } catch (Exception ex) {
        // throw ex;
        // }

        // 4° Intentar ejecutar el metodo para desactivar permiso de acceso por ID de
        // tablero e ID de usuario y establecer el rol anterior
        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        Role oldRole = operation.getOldRole();

        boardAccessWorkflowService.inactivateBoardAccessAndChangeRoleByBoardIdAndUserId(
                boardId, userId, oldRole);

        // 5° La compensación ha finalizado correctamente.
        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
