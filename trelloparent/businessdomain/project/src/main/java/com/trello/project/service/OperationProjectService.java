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

    List<Operation> findAllOperationsByInvitationIdAndTypeAndOrderByCreatedAtDesc(UUID invitationId,
            OperationType operationType);

    Optional<Operation> findOperationByMemberIdAndType(UUID operationId, OperationType operationType);

    // Guardar operación
    Operation saveOperation(Operation operation);

    List<Operation> findByMemberIdAndTypeAndStatusInOrderByCreatedAtDesc(UUID memberId,
            OperationType type,
            List<OperationStatus> statuses);

    List<Operation> findByInvitationIdAndTypeAndStatusInOrderByCreatedAtDesc(UUID invitationId,
            OperationType type,
            List<OperationStatus> statuses);

    Optional<Operation> findOperationByInvitationIdAndTypeAndStatus(UUID invitationId,
            OperationType type,
            List<OperationStatus> statuses);

    Optional<Operation> findLatestOperationByInvitationIdAndType(UUID invitationId,
            OperationType type);

    Optional<Operation> findLatestOperationByMemberIdAndType(UUID memberId, OperationType type);

    Optional<Operation> findLatestOperationByWorkspaceIdAndOwnerUserIdAndBoardNameAndType(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type);

    Optional<Operation> findLatestOperationByWorkspaceIdAndIdempotencyKeyAndType(UUID workspaceId, UUID idempotencyKey,
            OperationType type);

    Optional<Operation> findLatestOperationByBoardIdAndType(UUID boardId, OperationType type);
}
