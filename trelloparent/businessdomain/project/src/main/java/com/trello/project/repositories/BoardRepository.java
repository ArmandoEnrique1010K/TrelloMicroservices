package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.project.entities.Board;
import com.trello.project.enums.Role;

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

    // Verificar si el ID del usuario coincide con el ID del administrador del
    // tablero o uno de los miembros del tablero por ID

    // Si es miembro del tablero debe tener el rol de MEMBER o ADMIN
    @Query("""
                SELECT DISTINCT b
                FROM Board b
                LEFT JOIN b.members m
                WHERE b.id = :boardId
                AND (
                    b.workspace.ownerUserId = :userId
                    OR (
                        m.userId = :userId
                        AND m.role IN :roles
                    )
                )
            """)
    Optional<Board> findBoardAccessibleByUser(
            @Param("boardId") UUID boardId,
            @Param("userId") UUID userId,
            @Param("roles") Set<Role> roles);
}
