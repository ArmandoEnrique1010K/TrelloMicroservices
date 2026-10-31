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
import com.trello.workflow.exception.BoardAccessNotFoundException;
import com.trello.workflow.exception.BoardNotFoundException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

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
            throws OperationNotFoundException, BoardAccessAlreadyExistsException, BoardNotFoundException {
        // Recuperar la operación que se va a ejecutar
        Operation currentOperation = operationWorkflowService
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // Obtener los identificadores necesarios para localizar el permiso de acceso.
        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();

        // Rol nuevo
        Role newRole = currentOperation.getNewRole();

        // Obtiene tablero por ID
        Board board = boardWorkflowService.findBoardByIdAndStatusTrue(boardId);

        // Comprobar que el usuario todavía no tenga permiso de acceso al tablero.
        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserIdAndStatusTrue(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        // Crear el registro del nuevo permiso de acceso.
        BoardAccess savedBoardAccess = new BoardAccess();
        savedBoardAccess.setUserId(userId);
        savedBoardAccess.setRole(newRole);
        savedBoardAccess.setUserActive(true);
        savedBoardAccess.setBoard(board);
        savedBoardAccess.setStatus(true);

        // Persistir el permiso de acceso
        boardAccessWorkflowService.saveBoardAccess(savedBoardAccess);
    }

    // Revierte la operación de añadir un permiso de acceso al tablero.
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAddBoardAccess(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, BoardAccessNotFoundException {
        // Recuperar la operación que se va a compensar.
        Operation operation = operationWorkflowService
                .findOptionalOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // Comprobar que la operación corresponda al alta de un permiso de acceso.
        if (operation.getType() != OperationType.ADD_BOARD_ACCESS) {
            throw new OperationConflictException();
        }

        // Registrar que la operación se encuentra en proceso de compensación.
        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        // Obtener los identificadores necesarios para localizar el permiso de acceso.
        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();

        // Eliminar el permiso de acceso creado por la operación original.
        boardAccessWorkflowService.deleteBoardAccessByBoardIdAndUserIdAndStatusTrue(
                boardId, userId);

        // Registrar la finalización de la compensación.
        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
