package com.trello.workflow.services;

import java.util.Optional;
import java.util.UUID;

import com.trello.workflow.entities.Operation;

public interface OperationWorkflowService {

    // Devolver un Operation o un null si no existe la operacion realizada
    // por su ID
    Optional<Operation> findOperationById(UUID operationId);

    Operation saveOperation(Operation workflowOperation);
}
