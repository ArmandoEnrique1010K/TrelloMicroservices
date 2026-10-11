package com.trello.project.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationType;
import com.trello.project.repositories.OperationRepository;

@Service
public class OperationProjectServiceImpl implements OperationProjectService {

    private final OperationRepository operationRepository;

    public OperationProjectServiceImpl(
            OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Optional<Operation> findOperationById(UUID operationId) {
        Optional<Operation> operation = operationRepository.findById(operationId);
        return operation;
    }

    @Override
    public Operation saveOperation(Operation operation) {
        return operationRepository.save(operation);
    }

    @Override
    public Optional<Operation> findOperationByInvitationIdAndType(UUID operationId, OperationType operationType) {
        return operationRepository.findByInvitationIdAndType(operationId, operationType);
    }
}
