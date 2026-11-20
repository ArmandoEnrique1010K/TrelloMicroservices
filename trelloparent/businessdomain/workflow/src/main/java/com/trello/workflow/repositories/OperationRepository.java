package com.trello.workflow.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar operación por ID de tablero y ID de usuario
    Optional<Operation> findByBoardIdAndUserIdAndType(UUID boardId, UUID userId, OperationType type);

    // Listar todas las operaciones de un tablero
    List<Operation> findByBoardId(UUID boardId);

    // Actualizar el estado de todas las operaciones de un tablero
    @Modifying
    @Query("""
            UPDATE Operation o
            SET o.status=:status
            WHERE o.boardId=:boardId
            """)
    int updateStatusByBoardId(
            @Param("boardId") UUID boardId,
            @Param("status") OperationStatus status);

    // Actualizar el estado de todas las operaciones de varios tableros.
    @Modifying
    @Query("""
            UPDATE Operation o
            SET o.status = :status
            WHERE o.boardId IN :boardsIds
            """)
    int updateStatusByBoardsIds(
            @Param("boardsIds") List<UUID> boardsIds,
            @Param("status") OperationStatus status);

    @Modifying
    void deleteAllByStatus(OperationStatus status);

    @Modifying
    @Query("""
            UPDATE Operation o
            SET o.status=:status
            WHERE o.memberId=:memberId
            """)
    int updateStatusByMemberId(
            @Param("memberId") UUID memberId,
            @Param("status") OperationStatus status);

}