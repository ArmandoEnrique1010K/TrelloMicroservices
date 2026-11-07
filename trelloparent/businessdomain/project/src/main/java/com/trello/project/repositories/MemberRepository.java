package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Member;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    // Listar miembros por ID de tablero y ordenarlos de forma descendente por la
    // fecha en que se unio
    List<Member> findByBoardIdOrderByJoinedAtDesc(UUID boardId);

    List<Member> findByActiveTrueAndBoardIdOrderByJoinedAtDesc(UUID boardId);

    // boolean existsByBoardIdAndBoardWorkspaceOwnerUserIdAndRole(UUID boardId, UUID
    // ownerUserId, Role role);

    Optional<Member> findByIdAndBoardWorkspaceOwnerUserId(UUID id, UUID ownerUserId);

    // Metodo para buscar miembro por ID de tablero, ID de usuario y estado del
    // miembro en false
    Optional<Member> findByBoardIdAndUserIdAndActiveFalse(UUID boardId, UUID userId);

    Optional<Member> findByIdAndUserId(UUID id, UUID userId);

    Optional<Member> findByBoardIdAndUserId(UUID boardId, UUID userId);
}
