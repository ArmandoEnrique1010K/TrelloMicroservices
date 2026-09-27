package com.trello.workflow.board.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.entities.Board;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardWorkflowService boardWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;

    public BoardServiceImpl(
            BoardWorkflowService boardWorkflowService, BoardAccessWorkflowService boardAccessWorkflowService) {
        this.boardWorkflowService = boardWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
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
    @Override
    @Transactional(rollbackFor = BoardAlreadyExistsException.class)
    public void addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId) throws BoardAlreadyExistsException {

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
        boardAccess.setRole(Role.OWNER);
        boardAccess.setBoard(savedBoard);

        boardAccessWorkflowService.saveBoardAccess(boardAccess);
    }

    @Override
    public void deleteBoardByIdAndBoardAccessAndTasks(UUID boardId) {
        boardWorkflowService.deleteBoardById(boardId);
    }
}
