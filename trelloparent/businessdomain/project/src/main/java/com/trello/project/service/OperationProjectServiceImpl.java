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
    public Optional<Operation> findLatestOperationByInvitationIdAndType(UUID invitationId, OperationType type) {
        return operationRepository.findFirstByInvitationIdAndTypeOrderByCreatedAtDesc(invitationId, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByMemberIdAndType(UUID memberId, OperationType type) {
        return operationRepository.findFirstByMemberIdAndTypeOrderByCreatedAtDesc(memberId, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByWorkspaceIdAndOwnerUserIdAndBoardNameAndType(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type) {
        return operationRepository.findFirstByWorkspaceIdAndOwnerUserIdAndBoardNameAndTypeOrderByCreatedAtDesc(
                workspaceId, ownerUserId, boardName, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByWorkspaceIdAndIdempotencyKeyAndType(UUID workspaceId,
            UUID idempotencyKey, OperationType type) {
        return operationRepository.findFirstByWorkspaceIdAndIdempotencyKeyAndTypeOrderByCreatedAtDesc(workspaceId,
                idempotencyKey, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByBoardIdAndType(UUID boardId, OperationType type) {
        return operationRepository.findFirstByBoardIdAndTypeOrderByCreatedAtDesc(boardId, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByWorkspaceIdAndType(UUID workspaceId, OperationType type) {
        return operationRepository.findFirstByWorkspaceIdAndTypeOrderByCreatedAtDesc(workspaceId, type);
    }
}
