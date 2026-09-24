package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
    public void saveBoardAccess(UUID boardId, UUID userId, Role role) {

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

        // TODO: Agregar mensaje de error relacionado a que si tiene el rol de OWNER, no
        // puede cambiar su rol

        // Aunque esto es imposible porque el rol que se pasa desde el microservicio
        // Project no existe el rol de OWNER
        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setRole(role);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
    }
}
