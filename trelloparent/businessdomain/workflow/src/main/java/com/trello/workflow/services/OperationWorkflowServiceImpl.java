package com.trello.workflow.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.repositories.OperationRepository;

@Service
public class OperationWorkflowServiceImpl implements OperationWorkflowService {

    private final OperationRepository operationRepository;

    public OperationWorkflowServiceImpl(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    // Nota: En este caso como en ninguna de las operaciones se lanza la excepción
    // de OperationNotFoundException, en lugar de colocarlo en el paquete generico
    // "exceptions", se coloca en el paquete "operation.exception"
    @Override
    public Optional<Operation> findOperationById(UUID operationId) {
        return operationRepository.findById(operationId);
    }

    @Override
    public Operation saveOperation(Operation operation) {
        return operationRepository.save(operation);
    }

}
