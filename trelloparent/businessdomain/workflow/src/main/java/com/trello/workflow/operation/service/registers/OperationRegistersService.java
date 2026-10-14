package com.trello.workflow.operation.service.registers;

import java.util.UUID;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.Role;

public interface OperationRegistersService {
    // Registrar operación de agregar permiso de acceso
    Operation registerAddBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId);

    Operation registerActivateBoardAccess(UUID boardId, UUID userId, Role newRole, UUID operationId);
}
