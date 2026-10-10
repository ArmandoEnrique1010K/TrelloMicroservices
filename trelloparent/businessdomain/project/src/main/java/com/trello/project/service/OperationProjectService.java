package com.trello.project.service;

import java.util.Optional;
import java.util.UUID;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationType;

public interface OperationProjectService {
    // Ninguno de los métodos debe devolver una excepción

    // Devolver un Operation o un null si no existe la operacion realizada
    // por su ID
    Optional<Operation> findOperationById(UUID operationId);

    Optional<Operation> findOperationByInvitationIdAndType(UUID operationId, OperationType operationType);

    // Guardar operación
    Operation saveOperation(Operation operation);

    // Compensar operación
    void compensateOperation(UUID operationId);
}
