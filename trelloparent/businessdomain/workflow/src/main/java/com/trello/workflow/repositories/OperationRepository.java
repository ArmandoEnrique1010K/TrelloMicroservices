package com.trello.workflow.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar operación por ID de tablero y ID de usuario
    Optional<Operation> findByBoardIdAndUserIdAndType(UUID boardId, UUID userId, OperationType type);
}