package com.trello.workflow.boardaccess.service;

import java.util.UUID;

import com.trello.workflow.enums.Role;

public interface BoardAccessService {

    // Guardar datos de acceso al tablero
    // Cuando se crea un nuevo tablero primero se guarda al administrador del
    // espacio de trabajo como rol de OWNER
    // Por otro lado, cuando un invitado acepta una invitación tambien se agrega el
    // permiso de acceso
    void addBoardAccess(UUID boardId, UUID userId, Role role, UUID operationId);

    void activateBoardAccess(UUID boardId, UUID memberUserId, Role role, Role oldRole, UUID operationId);

    void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role role, Role oldRole, UUID operationId);

    void deactivateBoardAccess(UUID boardId, UUID memberUserId, Role oldRole, UUID operationId);
}
