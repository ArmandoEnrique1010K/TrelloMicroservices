package com.trello.project.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar y tomar la primera operación por ID de invitación y tipo de operación,
    // de una lista de operaciones ordenadas de forma descendente por fecha de
    // creación
    Optional<Operation> findFirstByInvitationIdAndTypeOrderByCreatedAtDesc(UUID invitationId, OperationType type);

    Optional<Operation> findFirstByMemberIdAndTypeOrderByCreatedAtDesc(UUID memberId, OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndOwnerUserIdAndBoardNameAndTypeOrderByCreatedAtDesc(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndIdempotencyKeyAndTypeOrderByCreatedAtDesc(UUID workspaceId,
            UUID idempotencyKey, OperationType type);

    Optional<Operation> findFirstByBoardIdAndTypeOrderByCreatedAtDesc(UUID boardId, OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndTypeOrderByCreatedAtDesc(UUID workspaceId, OperationType type);
}
