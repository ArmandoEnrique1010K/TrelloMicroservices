package com.trello.workflow.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.repositories.OperationRepository;

@Service
public class OperationWorkflowServiceImpl implements OperationWorkflowService {

    private final OperationRepository operationRepository;

    public OperationWorkflowServiceImpl(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Optional<Operation> findOperationById(UUID operationId) {
        return operationRepository.findById(operationId);
    }

    @Override
    public Optional<Operation> findOperationByBoardIdAndUserIdAndOperationType(UUID boardId, UUID userId,
            OperationType operationType) {
        return operationRepository.findByBoardIdAndUserIdAndType(boardId, userId, operationType);
    }

    @Override
    public Operation saveOperation(Operation operation) {
        return operationRepository.save(operation);
    }

}
