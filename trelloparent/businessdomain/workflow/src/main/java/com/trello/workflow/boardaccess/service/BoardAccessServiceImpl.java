package com.trello.workflow.boardaccess.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.service.processor.OperationProcessor;
import com.trello.workflow.operation.service.register.OperationRegisterService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final OperationProcessor operationProcessor;
    private final OperationRegisterService operationRegisterService;
    private final OperationWorkflowService operationWorkflowService;

    public BoardAccessServiceImpl(
            OperationProcessor operationProcessor,
            OperationRegisterService operationRegisterService,
            OperationWorkflowService operationWorkflowService) {
        this.operationProcessor = operationProcessor;
        this.operationRegisterService = operationRegisterService;
        this.operationWorkflowService = operationWorkflowService;
    }

    // Este método se llama desde un endpoint, donde el endpoint se llama desde el
    // microservicio Project.

    // Registra y procesa la operación de añadir un permiso de acceso al tablero.
    @Override
    public void addBoardAccess(UUID boardId, UUID userId, Role role,
            UUID operationId) {
        // Registrar la operación o recuperar una operación pendiente.
        Operation operation = operationRegisterService.registerAddBoardAccess(boardId, userId, role, operationId);

        // Incrementar el número de intentos de procesamiento.
        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        // Ejecutar la operación registrada.
        operationProcessor.processOperation(operation.getId());

        // Registrar la finalización de la operación.
        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

    // Activar un permiso de acceso al tablero
    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, Role role, Role oldRole,
            UUID operationId) {

        Operation operation = operationRegisterService.registerActivateBoardAccess(boardId, memberUserId,
                role,
                oldRole,
                operationId);

        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        operationProcessor.processOperation(operation.getId());

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

    @Override
    public void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role newRole, Role oldRole, UUID operationId) {
        Operation operation = operationRegisterService.registerChangeRoleBoardAccess(boardId, memberUserId,
                newRole,
                oldRole,
                operationId);

        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        operationProcessor.processOperation(operation.getId());

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }

    @Override
    public void deactivateBoardAccess(UUID boardId, UUID memberUserId, Role oldRole, UUID operationId) {
        Operation operation = operationRegisterService.registerDeactivateBoardAccess(boardId, memberUserId,
                oldRole,
                operationId);

        operation.setAttempts(operation.getAttempts() + 1);
        operationWorkflowService.saveOperation(operation);

        operationProcessor.processOperation(operation.getId());

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);
    }
}
