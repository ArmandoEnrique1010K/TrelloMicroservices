package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.project.entities.Member;
import com.trello.project.enums.Role;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    List<Member> findByBoardId(UUID boardId);

    boolean existsByBoardIdAndBoardWorkspaceOwnerUserIdAndRole(UUID boardId, UUID ownerUserId, Role role);

    Optional<Member> findByBoardIdAndBoardWorkspaceOwnerUserIdAndRole(UUID boardId, UUID ownerUserId, Role role);

    Optional<Member> findByIdAndBoardWorkspaceOwnerUserId(UUID boardId, UUID ownerUserId);
}
