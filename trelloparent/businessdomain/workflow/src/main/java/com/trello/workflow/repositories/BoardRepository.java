package com.trello.workflow.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.Board;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    boolean existsByIdAndStatusTrue(UUID id);

    Optional<Board> findByIdAndStatusTrue(UUID id);

    long countByIdIn(List<UUID> ids);

    Optional<Board> findByIdAndStatusFalse(UUID id);

    List<Board> findAllByIdInAndStatusTrue(List<UUID> ids);

    List<Board> findAllByIdInAndStatusFalse(List<UUID> ids);
}
