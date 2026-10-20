package com.trello.workflow.operation.service.register;

import java.util.UUID;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.Role;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotPendingException;

public interface OperationRegisterService {
    // Métodos que representan registros de operaciones

    // Agregar permiso de acceso
    Operation registerAddBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessAlreadyExistsException;

    // Activar permiso de acceso
    Operation registerActivateBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException;

    // Cambiar rol de permiso de acceso
    Operation registerChangeRoleBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException;

    // Desactivar permiso de acceso
    Operation registerDeactivateBoardAccess(UUID boardId, UUID userId, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BoardAccessUserActiveNotAllowedException;
}
