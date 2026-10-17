package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.service.procesors.OperationProcessor;
import com.trello.workflow.operation.service.registers.OperationRegistersService;

@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final OperationProcessor operationProcessor;
    private final OperationRegistersService operationRegistersService;

    public BoardAccessServiceImpl(
            BoardAccessWorkflowService boardAccessWorkflowService,
            OperationProcessor operationProcessor,
            OperationRegistersService operationRegistersService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.operationProcessor = operationProcessor;
        this.operationRegistersService = operationRegistersService;
    }

    @Override
    public void addBoardAccess(UUID boardId, UUID userId, Role role,
            UUID operationId) throws BoardAccessAlreadyExistsException {
        Operation operation = operationRegistersService.registerAddBoardAccess(boardId, userId, role, operationId);

        operationProcessor.processOperation(operation.getId());

        // try {
        // Operation operation =
        // operationRegistersService.registerAddBoardAccess(boardId, userId, role,
        // operationId);

        // operationProcessor.processOperation(operation.getId());

        // } catch (Exception e) {
        // operationProcessor.compensateOperation(operationId);
        // }
    }

    // @Transactional(rollbackFor = Exception.class)
    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, Role newRole,
            UUID operationId) {

        Operation operation = operationRegistersService.registerActivateBoardAccess(boardId, memberUserId,
                newRole,
                operationId);

        operationProcessor.processOperation(operation.getId());
        // Guardar el rol anterior
        // BoardAccess findedBoardAccess =
        // boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
        // boardId,
        // memberUserId);

        // ============================================================
        // Operación de negocio
        // ============================================================

        // El administrador del tablero no podra ser activado porque nunca se podra
        // desactivar
        // Este error nunca pasara porque el no se podra alterar un usuario con el rol
        // de OWNER
        // if (findedBoardAccess.getRole().equals(Role.OWNER)) {
        // throw new BusinessRuleException();
        // }

        // findedBoardAccess.setUserActive(true);
        // findedBoardAccess.setRole(role);
        // boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);

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
