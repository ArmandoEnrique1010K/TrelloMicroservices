package com.trello.workflow.boardaccess.service;

import java.util.UUID;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.boardaccess.exception.BoardAccessUserActiveNotAllowedException;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BoardAccessNotFoundException;
import com.trello.workflow.exception.BoardNotFoundException;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationConflictException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;

public interface BoardAccessService {

    // Guardar datos de acceso al tablero
    // Cuando se crea un nuevo tablero primero se guarda al administrador del
    // espacio de trabajo como rol de OWNER
    // Por otro lado, cuando un invitado acepta una invitación tambien se agrega el
    // permiso de acceso
    void addBoardAccess(UUID boardId, UUID userId, Role role, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            BoardAccessAlreadyExistsException, OperationNotFoundException, BoardNotFoundException;

    void activateBoardAccess(UUID boardId, UUID memberUserId, Role role, Role oldRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            BoardAccessUserActiveNotAllowedException, BoardAccessNotFoundException, OperationNotFoundException;

    void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role role, Role oldRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            BoardAccessUserActiveNotAllowedException, BoardAccessNotFoundException, OperationNotFoundException;

    void deactivateBoardAccess(UUID boardId, UUID memberUserId, Role oldRole, UUID operationId)
            throws OperationConflictException, OperationNotPendingException, BusinessRuleException,
            BoardAccessUserActiveNotAllowedException, BoardAccessNotFoundException, OperationNotFoundException;
}
