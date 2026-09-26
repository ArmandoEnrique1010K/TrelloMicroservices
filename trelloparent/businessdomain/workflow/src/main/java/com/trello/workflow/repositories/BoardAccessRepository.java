package com.trello.workflow.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;

public interface BoardAccessRepository extends JpaRepository<BoardAccess, UUID> {

    Optional<BoardAccess> findByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);

    Optional<BoardAccess> findByBoardIdAndUserIdAndRole(
            UUID boardId,
            UUID userId,
            Role role);

    // Aplicando @Modifying aqui, se evita hacer un SELECT para seleccionar todos
    // los registros que se van a eliminar y se opta por eliminar los registros sin
    // hacer un SELECT

    // Se ahorra 1 query menos
    // La cantidad de query que se ejecuta equivale a la cantidad de registros que
    // se van a eliminar
    @Modifying
    @Query("""
                DELETE FROM BoardAccess ba
                WHERE ba.boardId = :boardId
            """)
    int deleteByBoardId(@Param("boardId") UUID boardId);
}
