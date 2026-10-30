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
    Optional<Board> findByIdAndStatusTrue(UUID boardId);

    List<Board> findByWorkspaceIdAndStatusTrue(UUID workspaceId);

    boolean existsByWorkspaceIdAndNameAndStatusTrue(UUID workspaceId, String name);

    boolean existsByWorkspaceIdAndNameAndIdNotAndStatusTrue(
            UUID workspaceId,
            String name,
            UUID boardId);

    // Optional<Board> findByIdAndWorkspaceId(
    // UUID boardId,
    // UUID workspaceId);

    // Se utiliza para listar los miembros del tablero

    // Verificar si el ID del usuario coincide con el ID del administrador del
    // tablero o uno de los miembros del tablero por ID

    // Si es miembro del tablero debe tener el rol de MEMBER o ADMIN
    @Query("""
                SELECT DISTINCT b
                FROM Board b
                LEFT JOIN b.members m
                WHERE b.id = :boardId
                AND b.status = true
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

    // Listar todos los tableros por ID del usuario en que es miembro
    // Recordar que el usuario administrador del espacio de trabajo no sera miembro
    // porque es el administrador
    @Query("""
                SELECT b
                FROM Board b
                JOIN b.members m
                WHERE m.userId = :userId
                  AND b.status = true
                  AND m.active = true
            """)
    List<Board> findByMemberActiveTrueAndMemberUserId(@Param("userId") UUID userId);
}
