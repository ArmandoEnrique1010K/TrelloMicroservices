package com.trello.workflow.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.History;

public interface HistoryRepository extends JpaRepository<History, UUID> {
    List<History> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    @Modifying
    @Query("""
            DELETE FROM History h
            WHERE h.task.board.status = false
            AND h.task.board.deletedAt <= :expirationDate
            """)
    int deleteAllByBoardStatusFalseAndBoardDeletedAtBefore(
            @Param("expirationDate") LocalDateTime expirationDate);
}
