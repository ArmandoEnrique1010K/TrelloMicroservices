package com.trello.workflow.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.enums.Role;

public interface BoardAccessRepository extends JpaRepository<BoardAccess, UUID> {

    Optional<BoardAccess> findByBoardIdAndUserIdAndUserActiveTrueAndStatusTrue(UUID boardId, UUID userId);

    Optional<BoardAccess> findByBoardIdAndUserIdAndUserActiveFalseAndStatusTrue(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId);

    Optional<BoardAccess> findByBoardIdAndUserIdAndRoleAndStatusTrue(
            UUID boardId,
            UUID userId,
            Role role);

    Optional<BoardAccess> findByBoardIdAndUserIdAndStatusTrue(UUID boardId, UUID userId);

    // Obtener una lista de boardAccess por ID de tablero asociado
    // Sirve para cambiar el estado de todos ellos
    List<BoardAccess> findAllByBoardIdAndStatusTrue(UUID boardId);

    List<BoardAccess> findAllByBoardIdAndStatusFalse(UUID boardId);

    // Aplicando @Modifying aqui, se evita hacer un SELECT para seleccionar todos
    // los registros que se van a eliminar y se opta por eliminar los registros sin
    // hacer un SELECT

    // Se ahorra 1 query menos
    // La cantidad de query que se ejecuta equivale a la cantidad de registros que
    // se van a eliminar
    // @Modifying
    // @Query("""
    // DELETE FROM BoardAccess ba
    // WHERE ba.boardId = :boardId
    // """)
    // int deleteByBoardId(@Param("boardId") UUID boardId);
}
