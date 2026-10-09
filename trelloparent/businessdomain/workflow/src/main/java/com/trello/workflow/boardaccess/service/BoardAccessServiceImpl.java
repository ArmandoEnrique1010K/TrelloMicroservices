package com.trello.workflow.boardaccess.service;

import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

import java.time.LocalDateTime;
import java.util.Optional;
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
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationInProgressException;

@Service
public class BoardAccessServiceImpl implements BoardAccessService {

    private final OperationWorkflowService workflowOperationService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public BoardAccessServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService,
            BoardWorkflowService boardWorkflowService, OperationWorkflowService workflowOperationService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
        this.workflowOperationService = workflowOperationService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBoardAccess(UUID boardId, UUID userId, Role role,
            UUID operationId) throws BoardAccessAlreadyExistsException {

        // ============================================================
        // Idempotencia
        // ============================================================
        Optional<Operation> existingOperation = workflowOperationService.findOperationById(operationId);

        // La misma operación ya fue procesada anteriormente.
        if (existingOperation.isPresent()) {

            Operation operation = existingOperation.get();

            // La operación ya fue completada.
            if (operation.getStatus() == OperationStatus.COMPLETED) {
                return;
            }

            // La operación está siendo procesada.
            if (operation.getStatus() == OperationStatus.PROCESSING) {
                throw new OperationInProgressException();
            }

            // La operación ya fue compensada.
            if (operation.getStatus() == OperationStatus.COMPENSATED) {
                return;
            }
        }

        // ============================================================
        // Crear registro de operación
        // ============================================================
        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ADD_BOARD_ACCESS);
        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCreatedAt(LocalDateTime.now());
        operation.setBoardId(boardId);
        operation.setUserId(userId);

        // No se establece previousRole: el acceso todavía no existía.
        // operation.setPreviousRole(role);

        workflowOperationService.saveOperation(operation);

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

        // ============================================================
        // Operación completada
        // ============================================================
        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        workflowOperationService.saveOperation(operation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void activateBoardAccess(UUID boardId, UUID memberUserId, Role role,
            UUID operationId) {

        // ============================================================
        // Idempotencia
        // ============================================================

        Optional<Operation> optionalOperation = workflowOperationService.findOperationById(operationId);

        if (optionalOperation.isPresent()) {
            Operation operation = optionalOperation.get();

            // La operación ya fue completada.
            if (operation.getStatus() == OperationStatus.COMPLETED) {
                return;
            }

            // La operación está siendo procesada.
            if (operation.getStatus() == OperationStatus.PROCESSING) {
                throw new OperationInProgressException();
            }

            // La operación ya fue compensada.
            if (operation.getStatus() == OperationStatus.COMPENSATED) {
                return;
            }
        }

        // ============================================================
        // Crear operación
        // ============================================================

        Operation operation = new Operation();

        operation.setId(operationId);
        operation.setType(OperationType.ACTIVATE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCreatedAt(LocalDateTime.now());
        operation.setBoardId(boardId);
        operation.setUserId(memberUserId);

        // Guardar el rol anterior
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                memberUserId);

        operation.setPreviousRole(findedBoardAccess.getRole());

        workflowOperationService.saveOperation(operation);

        // ============================================================
        // Operación de negocio
        // ============================================================

        // El administrador del tablero no podra ser activado porque nunca se podra
        // desactivar
        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BusinessRuleException();
        }

        findedBoardAccess.setUserActive(true);
        findedBoardAccess.setRole(role);
        boardAccessWorkflowService.saveBoardAccess(findedBoardAccess);

        // ============================================================
        // Operación completada
        // ============================================================

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());

        workflowOperationService.saveOperation(operation);
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
