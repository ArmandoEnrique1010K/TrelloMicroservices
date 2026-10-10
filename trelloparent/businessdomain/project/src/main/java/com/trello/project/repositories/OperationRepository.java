package com.trello.project.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar operación por ID de invitación y Operation Type igual a
    // ACCEPT_INVITATION
    Optional<Operation> findByInvitationIdAndType(UUID invitationId, OperationType type);

}
