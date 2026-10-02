package com.trello.workflow.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.trello.workflow.entities.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Debido a la relación ManyToMany entre Task y Label, el JOIN FETCH puede
    // generar varias filas para una misma Task (una por cada Label). DISTINCT
    // evita que una misma Task aparezca repetida en el resultado. No evita que
    // un mismo Label pueda pertenecer a diferentes Tasks.
    @Query("""
                SELECT DISTINCT t
                FROM Task t
                LEFT JOIN FETCH t.labels
                JOIN FETCH t.board b
                WHERE b.id = :boardId
            """)
    List<Task> findAllByBoardIdWithLabels(UUID boardId);
}
