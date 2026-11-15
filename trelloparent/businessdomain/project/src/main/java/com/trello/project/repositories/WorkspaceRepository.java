package com.trello.project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.trello.project.entities.Workspace;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    // Listar tableros y ordenarlos de forma descendente por fecha de creación
    List<Workspace> findByOwnerUserIdAndStatusTrueOrderByCreatedAtDesc(UUID ownerUserId);

    boolean existsByOwnerUserIdAndNameAndStatusTrue(UUID ownerUserId, String name);

    boolean existsByOwnerUserIdAndNameAndIdNotAndStatusTrue(
            UUID ownerUserId,
            String name,
            UUID workspaceId);

    Optional<Workspace> findByIdAndOwnerUserIdAndStatusTrue(
            UUID workspaceId,
            UUID ownerUserId);

    Optional<Workspace> findByIdAndOwnerUserIdAndStatusFalse(
            UUID workspaceId,
            UUID ownerUserId);

    Optional<Workspace> findByIdAndOwnerUserId(
            UUID workspaceId,
            UUID ownerUserId);

    // Bloqueo pesimista cuando se busca un espacio de trabajo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Workspace w WHERE w.id = :workspaceId")
    Optional<Workspace> findByIdWithLock(UUID workspaceId);

}
