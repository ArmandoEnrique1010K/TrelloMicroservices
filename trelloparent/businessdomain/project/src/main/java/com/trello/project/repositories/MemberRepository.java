package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Member;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    List<Member> findByBoardId(UUID boardId);

    List<Member> findByActiveTrueAndBoardId(UUID boardId);

    // boolean existsByBoardIdAndBoardWorkspaceOwnerUserIdAndRole(UUID boardId, UUID
    // ownerUserId, Role role);

    Optional<Member> findByIdAndBoardWorkspaceOwnerUserId(UUID id, UUID ownerUserId);

    // Metodo para buscar miembro por ID de tablero, ID de usuario y estado del
    // miembro en false
    Optional<Member> findByBoardIdAndUserIdAndActiveFalse(UUID boardId, UUID userId);
}
