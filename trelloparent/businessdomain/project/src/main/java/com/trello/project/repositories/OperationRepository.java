package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar operación por ID de invitación y Tipo de operación
    Optional<Operation> findByInvitationIdAndType(UUID invitationId, OperationType type);

    Optional<Operation> findByInvitationIdAndTypeAndStatusNot(UUID invitationId,
            OperationType operationType,
            OperationStatus status);

    Optional<Operation> findByMemberIdAndType(UUID memberId, OperationType type);

    List<Operation> findByMemberIdAndTypeAndStatusInOrderByCreatedAtDesc(
            UUID memberId,
            OperationType type,
            List<OperationStatus> statuses);

    List<Operation> findByInvitationIdAndTypeAndStatusInOrderByCreatedAtDesc(
            UUID invitationId,
            OperationType type,
            List<OperationStatus> statuses);

    Optional<Operation> findByInvitationIdAndTypeAndStatusIn(
            UUID invitationId,
            OperationType type,
            List<OperationStatus> statuses);

    List<Operation> findByInvitationIdAndTypeOrderByCreatedAtDesc(
            UUID invitationId,
            OperationType type);

    Optional<Operation> findFirstByInvitationIdAndTypeOrderByCreatedAtDesc(UUID invitationId, OperationType type);

    Optional<Operation> findFirstByMemberIdAndTypeOrderByCreatedAtDesc(UUID memberId, OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndOwnerUserIdAndBoardNameAndTypeOrderByCreatedAtDesc(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndIdempotencyKeyAndTypeOrderByCreatedAtDesc(UUID workspaceId,
            UUID idempotencyKey, OperationType type);

    Optional<Operation> findFirstByBoardIdAndTypeOrderByCreatedAtDesc(UUID boardId, OperationType type);
}
