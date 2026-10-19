package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.service.processor.OperationProcessor;
import com.trello.workflow.operation.service.register.OperationRegisterService;

@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final OperationProcessor operationProcessor;
    private final OperationRegisterService operationRegisterService;

    public BoardAccessServiceImpl(
            BoardAccessWorkflowService boardAccessWorkflowService,
            OperationProcessor operationProcessor,
            OperationRegisterService operationRegisterService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.operationProcessor = operationProcessor;
        this.operationRegisterService = operationRegisterService;
    }

    // Añadir permiso de acceso
    // Se utiliza SAGA Pattern para procesar la operación
    @Override
    public void addBoardAccess(UUID boardId, UUID userId, Role role,
            UUID operationId) {
        // 1° Registrar la operación
        Operation operation = operationRegisterService.registerAddBoardAccess(boardId, userId, role, operationId);

        // 2° Procesar la operación registrada
        operationProcessor.processOperation(operation.getId());
    }

    // Activar permiso de acceso
    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, Role newRole,
            UUID operationId) {
        Operation operation = operationRegisterService.registerActivateBoardAccess(boardId, memberUserId,
                newRole,
                operationId);
        operationProcessor.processOperation(operation.getId());
    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role newRole, UUID operationId) {
        Operation operation = operationRegisterService.registerChangeRoleBoardAccess(boardId, memberUserId,
                newRole,
                operationId);

        operationProcessor.processOperation(operation.getId());

        // Buscar el boardAccess por ID de tablero e ID de usuario
        // El ID del usuario no es el mismo ID de miembro
        // BoardAccess findedBoardAccess =
        // boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
        // boardId,
        // memberUserId);

        // Aunque esto es imposible porque el rol que se pasa desde el microservicio
        // Project no existe el rol de OWNER en el enum que se encuentra en el
        // microservicio Project
        // if (findedBoardAccess.getRole().equals(Role.OWNER)) {
        // throw new BusinessRuleException();
        // }

        // findedBoardAccess.setRole(newRole);
        // boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);
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
