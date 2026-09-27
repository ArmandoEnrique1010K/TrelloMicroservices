package com.trello.workflow.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.Board;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    boolean existsById(UUID id);

    Optional<Board> findById(UUID id);
}
