package com.trello.workflow.boardaccess.service;

import java.util.UUID;

import com.trello.workflow.enums.Role;

public interface BoardAccessService {

    // Guardar datos de acceso al tablero
    // Cuando se crea un nuevo tablero primero se guarda al administrador del
    // espacio de trabajo como rol de OWNER
    // Tambien cuando un invitado acepta una invitación
    void saveBoardAccess(UUID boardId, UUID userId, Role role);

    void changeRoleBoardAccess(UUID boardId, UUID memberUserId, Role role);

    void deactivateBoardAccess(UUID boardId, UUID memberUserId);

    void activateBoardAccess(UUID boardId, UUID memberUserId, Role role);
}
