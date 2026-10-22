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

    // Registra la operación antes de ejecutar el efecto
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAddBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessAlreadyExistsException {

        // En el caso de que no exista la operación se procede a crearla
        validateExistingOperationOrReturnPending(operationId, boardId, userId, newRole, null,
                OperationType.ADD_BOARD_ACCESS);

        // Validar reglas de negocio
        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        // Crear una nueva operación
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

        operationWorkflowService.saveOperation(operation);

        // Retornar la entidad completa de la operación
        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerActivateBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {

        validateExistingOperationOrReturnPending(operationId, boardId, userId, newRole, null,
                OperationType.ACTIVATE_BOARD_ACCESS);

        // Validar reglas de negocio
        BoardAccess findedBoardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserInactive(
                boardId,
                userId);

        if (findedBoardAccess.getRole().equals(Role.OWNER)) {
            throw new BoardAccessUserActiveNotAllowedException();
        }

        // Crear una nueva operación
        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACTIVATE_BOARD_ACCESS);
        operation.setStatus(OperationStatus.CREATED);
        operation.setBoardId(boardId);
        operation.setUserId(userId);

        // Si o si va a tener un valor false
        operation.setOldUserActive(findedBoardAccess.isUserActive());

        // Toma el rol antiguo y el nuevo
        operation.setOldRole(findedBoardAccess.getRole());
        operation.setNewRole(newRole);

        operation.setAttempts(0);

        operationWorkflowService.saveOperation(operation);

        // Retornar la entidad completa de la operación
        return operation;
    }

    @Override
    public Operation registerChangeRoleBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {

        validateExistingOperationOrReturnPending(operationId, boardId, userId, newRole, null,
                OperationType.CHANGE_ROLE_BOARD_ACCESS);

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

        operation.setOldUserActive(findedBoardAccess.isUserActive());
        operation.setOldRole(findedBoardAccess.getRole());
        operation.setNewRole(newRole);

        operation.setAttempts(0);
        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    @Override
    public Operation registerDeactivateBoardAccess(UUID boardId, UUID userId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException {
        validateExistingOperationOrReturnPending(operationId, boardId, userId, null, null,
                OperationType.DEACTIVATE_BOARD_ACCESS);

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

        operation.setOldUserActive(findedBoardAccess.isUserActive());
        operation.setOldRole(findedBoardAccess.getRole());

        operation.setAttempts(0);
        operationWorkflowService.saveOperation(operation);

        return operation;
    }

    // Método privado para validar la operación existente y devolver aquella
    // operación o devolver un null
    private Operation validateExistingOperationOrReturnPending(
            UUID operationId,
            UUID boardId,
            UUID userId,
            Role newRole,
            List<UUID> boardsIds,
            OperationType expectedType) throws OperationConflictException, OperationNotPendingException {

        // Busca una operación existente en la base de datos
        Optional<Operation> existingOperation = operationWorkflowService.findOperationById(operationId);

        // Si no existe la operación
        if (existingOperation.isEmpty()) {
            return null;
        }

        Operation existing = existingOperation.get();

        // Impide reutilizar el mismo identificador para una operación diferente
        if (existing.getType() != expectedType
                || !existing.getBoardId().equals(boardId)
                // El id del usuario es opcional
                || (userId != null && !existing.getUserId().equals(userId))
                // El nuevo rol es opcional, si no se pasa un rol, se ignora esta ultima
                // condición
                || (newRole != null && existing.getNewRole() != newRole)
                // La lista de boardsIds es opcional, se utiliza en un solo metodo
                || (boardsIds != null && existing.getBoardIds() != boardsIds)) {
            // Excepción de una operación conflictiva
            throw new OperationConflictException();
        }

        // Un reintento de una operación ya completada no debe volver a ejecutar el
        // alta
        switch (existing.getStatus()) {
            case PENDING -> {
                return existing;
            }

            // Estos casos representan que la operacion no esta pendiente
            case CREATED, PROCESSING, COMPLETED, MANUAL_REVIEW -> {
                throw new OperationNotPendingException();
            }

            // Este ultimo caso nunca llegara a pasar
            default -> throw new BusinessRuleException();
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAddBoardAndBoardAccessUserOwner(UUID boardId, UUID userId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAlreadyExistsException {
        validateExistingOperationOrReturnPending(operationId, boardId, userId, null, null,
                OperationType.ADD_BOARD_AND_BOARD_ACCESS_USER_OWNER);

        // Logica de negocio, verificar si existe
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
        validateExistingOperationOrReturnPending(operationId, boardId, null, null, null,
                OperationType.DELETE_BOARD_AND_BOARD_ACCESS_AND_TASKS);

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

        validateExistingOperationOrReturnPending(operationId, null, null, null,
                boardsIds, OperationType.DELETE_ALL_BOARDS_AND_BOARD_ACCESS_AND_TASKS);

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

        // Lista de IDs de tableros
        operation.setBoardIds(boardsIds);

        operation.setAttempts(0);
        operationWorkflowService.saveOperation(operation);

        return operation;
    }

}
