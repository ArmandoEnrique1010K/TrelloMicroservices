package com.trello.workflow.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.Board;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    boolean existsByIdAndStatusTrue(UUID id);

    Optional<Board> findByIdAndStatusTrue(UUID id);

    long countByIdIn(List<UUID> ids);

    Optional<Board> findByIdAndStatusFalse(UUID id);

    List<Board> findAllByIdInAndStatusTrue(List<UUID> ids);

    List<Board> findAllByIdInAndStatusFalse(List<UUID> ids);

    // Eliminar físicamente los tableros cuyo estado sea false y cuya fecha
    // de eliminación haya superado el período de retención establecido.

    // clearAutomatically sirve para limpiar el contexto despues de cada eliminación
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM Board b
            WHERE b.status = false
            AND b.deletedAt <= :expirationDate
            """)
    int deleteAllByStatusFalseAndDeletedAtBefore(
            @Param("expirationDate") LocalDateTime expirationDate);
}
