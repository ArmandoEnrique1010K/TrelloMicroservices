package com.trello.project.service;

import java.time.LocalDateTime;
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

    // Guardar operación
    Operation saveOperation(Operation operation);

    Optional<Operation> findLatestOperationByInvitationIdAndType(UUID invitationId,
            OperationType type);

    Optional<Operation> findLatestOperationByMemberIdAndType(UUID memberId, OperationType type);

    Optional<Operation> findLatestOperationByWorkspaceIdAndOwnerUserIdAndBoardNameAndType(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type);

    Optional<Operation> findLatestOperationByWorkspaceIdAndIdempotencyKeyAndType(UUID workspaceId, UUID idempotencyKey,
            OperationType type);

    Optional<Operation> findLatestOperationByBoardIdAndType(UUID boardId, OperationType type);

    Optional<Operation> findLatestOperationByWorkspaceIdAndType(UUID workspaceId, OperationType type);

    List<Operation> findOperationsByStatusesListOrderByNextAutomaticAttemptAtAscWorkspaceIdAsc(
            List<OperationStatus> statuses);

    boolean existsPreviousOperationsByWorkspaceIdAndCreatedAt(UUID workspaceId, LocalDateTime createdAt);

    List<Operation> findOperationsByOperationTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
            OperationType operationType, OperationStatus operationStatus);
}
