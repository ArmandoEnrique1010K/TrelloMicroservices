package com.trello.workflow.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.BoardAccess;

public interface BoardAccessRepository extends JpaRepository<BoardAccess, UUID> {

    Optional<BoardAccess> findByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);
}
