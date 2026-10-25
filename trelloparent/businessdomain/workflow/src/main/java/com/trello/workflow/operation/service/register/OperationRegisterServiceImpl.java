package com.trello.workflow.operation.service.register;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.board.exception.BoardAlreadyExistsException;
import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BoardNotFoundException;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationRegisterServiceImpl implements OperationRegisterService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationRegisterServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService,
            BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    // Registra la operación de añadir un permiso de acceso antes de ejecutar el
    // efecto.
    // Si existe una operación pendiente, devuelve la operación registrada para
    // permitir su reintento.
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAddBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessAlreadyExistsException {

        // Comprobar si existe una operación con el identificador proporcionado.
        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                boardId,
                userId,
                newRole,
                null,
                OperationType.ADD_BOARD_ACCESS);

        // Si existe una operación pendiente, devolverla para continuar su
        // procesamiento.
        if (existingOperation != null) {
            existingOperation.setNewRole(newRole);
            return operationWorkflowService.saveOperation(existingOperation);
        }

        // Comprobar que el usuario todavía no tenga permiso de acceso al tablero.
        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        // Crear el registro de la nueva operación.
        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ADD_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setOldUserActive(null);
        operation.setOldRole(null);
        operation.setNewRole(newRole);
        operation.setAttempts(0);

        // Persistir la operación antes de ejecutar el cambio de negocio.
        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerActivateBoardAccess(UUID boardId, UUID userId, Role newRole, Role oldRole,
            UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {

        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                boardId,
                userId,
                newRole,
                null,
                OperationType.ACTIVATE_BOARD_ACCESS);

        if (existingOperation != null) {
            existingOperation.setOldRole(oldRole);
            existingOperation.setNewRole(newRole);
            return operationWorkflowService.saveOperation(existingOperation);
        }

        // Validar reglas de negocio
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACTIVATE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setOldUserActive(false);
        operation.setOldRole(oldRole);
        operation.setNewRole(newRole);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    @Override
    public Operation registerChangeRoleBoardAccess(UUID boardId, UUID userId, Role newRole, Role oldRole,
            UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {

        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                boardId,
                userId,
                newRole,
                null,
                OperationType.CHANGE_ROLE_BOARD_ACCESS);

        if (existingOperation != null) {
            existingOperation.setOldRole(oldRole);
            existingOperation.setNewRole(newRole);
            return operationWorkflowService.saveOperation(existingOperation);
        }

        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.CHANGE_ROLE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setOldUserActive(true);
        operation.setOldRole(oldRole);
        operation.setNewRole(newRole);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    // Valida una operación existente y devuelve la operación si está pendiente.
    // Si no existe, devuelve null para permitir el registro de una nueva operación.
    private Operation validateExistingOperationOrReturnPending(
            UUID operationId,
            UUID boardId,
            UUID userId,
            Role newRole,
            List<UUID> boardsIds,
            OperationType expectedType) throws OperationConflictException, OperationNotPendingException {

        // Buscar la operación mediante su identificador.
        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        // Si no existe una operación con ese identificador, permitir su registro.
        if (existingOperation.isEmpty()) {
            return null;
        }

        Operation existing = existingOperation.get();

        // Comprobar que los datos de la solicitud coincidan con la operación
        // registrada.
        if (existing.getType() != expectedType
                || !existing.getBoardId().equals(boardId)
                // El id del usuario es opcional
                || (userId != null && !existing.getUserId().equals(userId))
                // La lista de boardsIds es opcional, se utiliza en un solo metodo
                || (boardsIds != null && existing.getBoardIds() != boardsIds)) {
            // Excepción de una operación conflictiva
            throw new OperationConflictException();
        }

        // Determinar si la operación puede volver a procesarse según su estado actual.
        switch (existing.getStatus()) {
            case PENDING -> {
                // Actualizar el rol por si ha cambiado desde el registro inicial.
                existing.setNewRole(newRole);

                // La lista de IDs de tableros tambien cambian
                existing.setBoardIds(boardsIds);
                return existing;
            }

            // Impedir el reintento de operaciones que no están pendientes.
            case CREATED, PROCESSING, COMPLETED, MANUAL_REVIEW -> {
                throw new OperationNotPendingException();
            }

            // Rechazar cualquier estado que no esté contemplado en el flujo.
            // Este ultimo caso nunca llegara a pasar
            default -> throw new BusinessRuleException();
        }

    }

    @Override
    public Operation registerDeactivateBoardAccess(UUID boardId, UUID userId, Role newRole, Role oldRole,
            UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {

        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                boardId,
                userId,
                newRole,
                null,
                OperationType.DEACTIVATE_BOARD_ACCESS);

        if (existingOperation != null) {
            // Si cambia el rol del usuario en el caso de que los datos del microservicio
            // Project no coincidan con el rol establecido en la tabla BoardAccess
            // existingOperation.setNewRole(newRole);
            return operationWorkflowService.saveOperation(existingOperation);
        }

        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.DEACTIVATE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setOldUserActive(true);
        operation.setOldRole(oldRole);
        operation.setNewRole(newRole);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    // TODO: CONTINUAR CON LOS METODOS RELACIONADOS A BOARD
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAddBoardAndBoardAccessUserOwner(UUID boardId, UUID userId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAlreadyExistsException {

        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                boardId,
                userId,
                null,
                null,
                OperationType.ADD_BOARD_AND_BOARD_ACCESS_USER_OWNER);

        if (existingOperation != null) {
            return operationWorkflowService.saveOperation(existingOperation);
        }

        // Verificar si existe el tablero por ID
        if (boardWorkflowService.existsBoardById(boardId)) {
            throw new BoardAlreadyExistsException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ADD_BOARD_AND_BOARD_ACCESS_USER_OWNER);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setNewRole(Role.OWNER);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerDeleteBoardAndBoardAccessAndTasks(UUID boardId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardNotFoundException {

        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                boardId,
                null,
                null,
                null,
                OperationType.DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS);

        if (existingOperation != null) {
            return operationWorkflowService.saveOperation(existingOperation);
        }

        // Verificar si no existe el tablero
        if (!boardWorkflowService.existsBoardById(boardId)) {
            throw new BoardNotFoundException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerDeleteAllBoardsAndBoardAccessAndTasks(List<UUID> boardsIds, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardNotFoundException {

        Operation existingOperation = validateExistingOperationOrReturnPending(
                operationId,
                null,
                null,
                null,
                boardsIds, OperationType.DELETE_ALL_BOARDS_AND_BOARD_ACCESS_AND_TASKS);

        if (existingOperation != null) {
            // Aqui cambia la lista de IDs de tableros
            existingOperation.setBoardIds(boardsIds);
            return operationWorkflowService.saveOperation(existingOperation);
        }

        // Aunque esto nunca va a pasar
        // Si la cantidad de tableros no coinciden con los tableros por IDs
        if (boardWorkflowService.countBoardsByIds(boardsIds) != boardsIds.size()) {
            // TODO: Cambiar la excepcion por uno más detallista
            throw new BoardNotFoundException();
        }

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardIds(boardsIds);
        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        return operation;
    }
}
