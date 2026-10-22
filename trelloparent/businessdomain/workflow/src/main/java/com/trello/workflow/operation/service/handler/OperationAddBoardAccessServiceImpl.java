package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.entities.Board;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationAddBoardAccessServiceImpl implements OperationAddBoardAccessService {

    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationAddBoardAccessServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService,
            BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyAddBoardAccess(UUID operationId)
            throws OperationNotFoundException, BoardAccessAlreadyExistsException {
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

        // Rol nuevo
        Role newRole = currentOperation.getNewRole();

        // Buscar por tablero
        Board board = boardWorkflowService.findBoardById(boardId);

        // 3° Volver a preguntar si existe permiso de acceso
        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        // 4° Añadir el nuevo permiso de acceso
        BoardAccess savedBoardAccess = new BoardAccess();
        savedBoardAccess.setUserId(userId);
        savedBoardAccess.setRole(newRole);
        savedBoardAccess.setUserActive(true);
        savedBoardAccess.setBoard(board);
        savedBoardAccess.setStatus(true);

        boardAccessWorkflowService.saveBoardAccess(savedBoardAccess);

        // 5° Marcar la operación como completada
        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAddBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationNotPendingException {
        // 1° Buscar operación
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // 2° Verificar el tipo de operación
        if (operation.getType() != OperationType.ADD_BOARD_ACCESS) {
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

        // 4° Intentar ejecutar el metodo para borrar permiso de acceso por ID de
        // tablero e ID de usuario
        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();

        boardAccessWorkflowService.deleteBoardAccessByBoardIdAndUserId(
                boardId, userId);

        // try {
        // boardAccessWorkflowService.deleteBoardAccessByBoardIdAndUserId(operation.getBoardId(),
        // operation.getUserId());
        // } catch (RuntimeException exception) {
        // operation.setStatus(OperationStatus.MANUAL_REVIEW);
        // operation.setCompletedAt(null);
        // operationWorkflowService.saveOperation(operation);
        // } catch (Exception ex) {
        // throw ex;
        // }

        // 5° La compensación ha finalizado correctamente.
        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
