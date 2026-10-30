package com.trello.project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Workspace;
import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByOwnerUserIdAndStatusTrue(UUID ownerUserId);

    boolean existsByOwnerUserIdAndNameAndStatusTrue(UUID ownerUserId, String name);

    boolean existsByOwnerUserIdAndNameAndIdNotAndStatusTrue(
            UUID ownerUserId,
            String name,
            UUID workspaceId);

    Optional<Workspace> findByIdAndOwnerUserIdAndStatusTrue(
            UUID workspaceId,
            UUID ownerUserId);

    Optional<Workspace> findByIdAndOwnerUserId(
            UUID workspaceId,
            UUID ownerUserId);

}
