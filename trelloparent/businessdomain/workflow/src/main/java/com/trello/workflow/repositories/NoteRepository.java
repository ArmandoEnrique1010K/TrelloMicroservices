package com.trello.workflow.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.Note;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    @Modifying
    @Query("""
            DELETE FROM Note n
            WHERE n.task.board.status = false
            AND n.task.board.deletedAt <= :expirationDate
            """)
    int deleteAllByBoardStatusFalseAndBoardDeletedAtBefore(
            @Param("expirationDate") LocalDateTime expirationDate);
}
