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

    @Override
    public Optional<Operation> findOperationById(UUID operationId) {
        Optional<Operation> workflowOperation = operationRepository.findById(operationId);
        return workflowOperation;
    }

    @Override
    public Operation saveOperation(Operation workflowOperation) {
        return operationRepository.save(workflowOperation);
    }
}
