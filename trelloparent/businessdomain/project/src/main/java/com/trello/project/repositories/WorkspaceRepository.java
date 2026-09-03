package com.trello.project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Workspace;
import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByOwnerUserId(UUID ownerUserId);

    boolean existsByOwnerUserIdAndName(UUID ownerUserId, String name);

    boolean existsByOwnerUserIdAndNameAndIdNot(
            UUID ownerUserId,
            String name,
            UUID workspaceId);

    Optional<Workspace> findByIdAndOwnerUserId(
            UUID workspaceId,
            UUID ownerUserId);
}
