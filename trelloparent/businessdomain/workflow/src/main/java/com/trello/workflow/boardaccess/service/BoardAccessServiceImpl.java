package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;

@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final BoardAccessWorkflowService boardAccessWorkflowService;

    BoardAccessServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
    }

    @Override
    public void addBoardAccess(UUID boardId, UUID userId, Role role) {

        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        BoardAccess savedBoardAccess = new BoardAccess();
        savedBoardAccess.setUserId(userId);
        savedBoardAccess.setBoardId(boardId);
        savedBoardAccess.setRole(role);
        savedBoardAccess.setUserActive(true);

        boardAccessWorkflowService.saveBoardAccess(savedBoardAccess);
    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role role) {

        // Buscar el boardAccess por ID de tablero e ID de usuario
        // El ID del usuario no es el mismo ID de miembro
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserId(boardId,
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
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserId(boardId,
                memberUserId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setUserActive(false);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
    }

    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, Role role) {
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserId(boardId,
                memberUserId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setUserActive(true);
        findedBoardAccess.setRole(role);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);

    }

    // En este caso @Transactional se va a encargar de ejecutar ambos metodos del
    // servicios en una sola transaccion
    @Transactional
    @Override
    public void deleteAllBoardAccessByBoardId(UUID boardId, UUID memberOwneruserId) {
        boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndRoleOwner(boardId, memberOwneruserId);
        boardAccessWorkflowService.deleteAllBoardAccessByBoardId(boardId);
    }
}
