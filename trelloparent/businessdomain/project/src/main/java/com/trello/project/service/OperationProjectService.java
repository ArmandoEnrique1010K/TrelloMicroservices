package com.trello.project.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;

public interface OperationProjectService {
    // Ninguno de los métodos debe devolver una excepción

    // Devolver un Operation o un null si no existe la operacion realizada
    // por su ID
    Optional<Operation> findOperationById(UUID operationId);

    Optional<Operation> findOperationByInvitationIdAndType(UUID invitationId, OperationType operationType);

    Optional<Operation> findOperationByMemberIdAndType(UUID operationId, OperationType operationType);

    // Guardar operación
    Operation saveOperation(Operation operation);

    List<Operation> findByMemberIdAndTypeAndStatusInOrdenByCreatedAtDesc(UUID memberId,
            OperationType type,
            List<OperationStatus> statuses);
}
