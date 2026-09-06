package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Board;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByWorkspaceId(UUID workspaceId);

    boolean existsByWorkspaceIdAndName(UUID workspaceId, String name);

    boolean existsByWorkspaceIdAndNameAndIdNot(
            UUID workspaceId,
            String name,
            UUID boardId);

    Optional<Board> findByIdAndWorkspaceId(
            UUID boardId,
            UUID workspaceId);
}
