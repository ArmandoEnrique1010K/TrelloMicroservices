package com.trello.workflow.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;
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

@Service
public class OperationAddBoardAndBoardAccessUserOwnerServiceImpl
        implements OperationAddBoardAndBoardAccessUserOwnerService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationAddBoardAndBoardAccessUserOwnerServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService, BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    // En este caso @Transactional se va a encargar de ejecutar ambos metodos del
    // servicios en una sola transaccion

    // @Transactional ejecuta las operaciones de persistencia dentro de una misma
    // transacción. Si ocurre una excepción que provoque rollback, se revierten
    // los cambios realizados durante dicha transacción.

    // rollbackFor permite indicar que BoardAlreadyExistsException también debe
    // provocar un rollback, incluso si es una excepción checked.

    // De esta manera, si el Board se guarda correctamente pero posteriormente
    // falla el guardado de BoardAccess con una excepción que provoque rollback,
    // el guardado del Board también será revertido.

    // No se agrega BoardAlreadyExistsException.class porque es redundante, ya que
    // hereda de RuntimeException
    // @Transactional(rollbackFor = { BoardAlreadyExistsException.class,
    // Exception.class })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyAddBoardAndBoardAccessUserOwner(UUID operationId)
            throws BoardAlreadyExistsException, OperationNotFoundException {
        Operation currentOperation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        currentOperation.setStatus(OperationStatus.PROCESSING);
        currentOperation.setAttempts(currentOperation.getAttempts() + 1);
        operationWorkflowService.saveOperation(currentOperation);

        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();
        // OWNER
        Role newRole = currentOperation.getNewRole();

        // if (boardWorkflowService.existsBoardById(boardId)) {
        // throw new BoardAlreadyExistsException();
        // }

        Board board = new Board();
        board.setId(boardId);

        Board savedBoard;

        // Se intenta guardar directamente sin comprobar previamente si el Board existe.
        // La clave primaria de la base de datos garantiza que no puedan existir
        // dos Boards con el mismo ID. Si el ID ya existe, la base de datos genera
        // una violación de integridad que se convierte en BoardAlreadyExistsException.
        try {
            savedBoard = boardWorkflowService.saveAndFlushBoard(board);
        } catch (DataIntegrityViolationException ex) {
            throw new BoardAlreadyExistsException();
        }

        // También debe añadir un permiso de acceso por parte del administrador
        // del espacio de trabajo, que a su vez es el administrador del tablero creado
        BoardAccess boardAccess = new BoardAccess();
        boardAccess.setUserId(userId);
        boardAccess.setUserActive(true);
        boardAccess.setRole(newRole);
        boardAccess.setBoard(savedBoard);

        boardAccessWorkflowService.saveBoardAccess(boardAccess);

        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAddBoardAndBoardAccessUserOwner(UUID operationId) {
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.ADD_BOARD_AND_BOARD_ACCESS_USER_OWNER) {
            throw new OperationConflictException();
        }

        if (operation.getStatus() == OperationStatus.CREATED
                && operation.getStatus() == OperationStatus.COMPLETED
                && operation.getStatus() == OperationStatus.MANUAL_REVIEW) {
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        // Role newRole = operation.getNewRole();

        // Revertir los cambios en la base de datos
        // Eliminar el permiso de acceso del usuario administrador y el tablero
        boardWorkflowService.deleteBoardByIdAndDeleteBoardAccessByBoardIdAndUserId(
                boardId, userId);

        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
