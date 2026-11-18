package com.trello.workflow.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Debido a la relación ManyToMany entre Task y Label, el JOIN FETCH puede
    // generar varias filas para una misma Task (una por cada Label). DISTINCT
    // evita que una misma Task aparezca repetida en el resultado. No evita que
    // un mismo Label pueda pertenecer a diferentes Tasks.

    // Ademas se van a ordenar de forma descendente por fecha de actualización
    @Query("""
                SELECT DISTINCT t
                FROM Task t
                LEFT JOIN FETCH t.labels
                JOIN FETCH t.board b
                WHERE b.id = :boardId
                ORDER BY t.updatedAt DESC
            """)
    List<Task> findAllByBoardIdWithLabelsOrderByUpdatedAtDesc(UUID boardId);

    @Modifying
    @Query("""
            DELETE FROM Task t
            WHERE t.board.id IN (
                SELECT b.id FROM Board b
                WHERE b.status = false
                AND b.deletedAt <= :expirationDate
            )
            """)
    int deleteAllByBoardsPendingPhysicalDeletion(
            @Param("expirationDate") LocalDateTime expirationDate);

    @Modifying
    @Query(value = """
            DELETE FROM tasks_labels
            WHERE task_id IN (
                SELECT t.id
                FROM tasks t
                INNER JOIN boards b ON b.id = t.board_id
                WHERE b.status = false
                AND b.deleted_at <= :expirationDate
            )
            """, nativeQuery = true)
    int deleteAllTaskLabelRelationsByBoardStatusFalseAndBoardDeletedAtBefore(
            @Param("expirationDate") LocalDateTime expirationDate);

    @Modifying
    @Query("""
            DELETE FROM Task t
            WHERE t.board.status = false
            AND t.board.deletedAt <= :expirationDate
            """)
    int deleteAllByBoardStatusFalseAndBoardDeletedAtBefore(
            @Param("expirationDate") LocalDateTime expirationDate);
}
