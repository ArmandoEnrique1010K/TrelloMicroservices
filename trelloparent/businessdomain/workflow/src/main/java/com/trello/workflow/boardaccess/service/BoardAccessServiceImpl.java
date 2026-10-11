package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.entities.Board;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;

@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public BoardAccessServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService,
            BoardWorkflowService boardWorkflowService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBoardAccess(UUID boardId, UUID userId, Role role,
            UUID operationId) throws BoardAccessAlreadyExistsException {

        // ============================================================
        // Operación de negocio
        // ============================================================
        Board board = boardWorkflowService.findBoardById(boardId);

        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        BoardAccess savedBoardAccess = new BoardAccess();
        savedBoardAccess.setUserId(userId);
        savedBoardAccess.setRole(role);
        savedBoardAccess.setUserActive(true);
        savedBoardAccess.setBoard(board);

        boardAccessWorkflowService.saveBoardAccess(savedBoardAccess);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, Role role,
            UUID operationId) {

        // Guardar el rol anterior
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                memberUserId);

        // ============================================================
        // Operación de negocio
        // ============================================================

        // El administrador del tablero no podra ser activado porque nunca se podra
        // desactivar
        // Este error nunca pasara porque el no se podra alterar un usuario con el rol
        // de OWNER
        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setUserActive(true);
        findedBoardAccess.setRole(role);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);

    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role role) {

        // Buscar el boardAccess por ID de tablero e ID de usuario
        // El ID del usuario no es el mismo ID de miembro
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
                boardId,
                memberUserId);

        // Aunque esto es imposible porque el rol que se pasa desde el microservicio
        // Project no existe el rol de OWNER en el enum que se encuentra en el
        // microservicio Project
        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setRole(role);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
    }

    @Override
    public void deactivateBoardAccess(UUID boardId, UUID memberUserId) {
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
                boardId,
                memberUserId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setUserActive(false);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
    }
}
