package com.trello.project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.project.entities.Board;
import com.trello.project.enums.BoardStatus;
import com.trello.project.enums.Role;

import jakarta.persistence.LockModeType;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    // Optional<Board> findByIdAndStatusTrue(UUID boardId);

    Optional<Board> findByIdAndStatus(UUID boardId, BoardStatus status);

    // Listar todos los tableros por ID de espacio de trabajo y estado true
    // Ordenar de forma descendente (mayor a menor) por fecha de creación
    // List<Board> findByWorkspaceIdAndStatusTrueOrderByCreatedAtDesc(UUID
    // workspaceId);

    List<Board> findByWorkspaceIdAndStatusOrderByCreatedAtDesc(UUID workspaceId, BoardStatus status);

    // boolean existsByWorkspaceIdAndNameAndStatusTrue(UUID workspaceId, String
    // name);

    boolean existsByWorkspaceIdAndNameAndStatus(UUID workspaceId, String name, BoardStatus status);

    // boolean existsByWorkspaceIdAndNameAndIdNotAndStatusTrue(
    // UUID workspaceId,
    // String name,
    // UUID boardId);
    boolean existsByWorkspaceIdAndNameAndIdNotAndStatus(
            UUID workspaceId,
            String name,
            UUID boardId,
            BoardStatus status);

    // Se utiliza para listar los miembros del tablero

    // Verificar si el ID del usuario coincide con el ID del administrador del
    // tablero o uno de los miembros del tablero por ID

    // Si es miembro del tablero debe tener el rol de MEMBER o ADMIN
    @Query("""
                SELECT DISTINCT b
                FROM Board b
                LEFT JOIN b.members m
                WHERE b.id = :boardId
                AND b.status = :status
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
            @Param("roles") Set<Role> roles,
            @Param("status") BoardStatus status);

    // Listar todos los tableros por ID del usuario en que es miembro
    // Recordar que el usuario administrador del espacio de trabajo no sera miembro
    // porque es el administrador

    // Ordenar por fecha en la que el miembro se unio de forma descendente
    // m.joinedAt es de tipo LocalDateTime
    @Query("""
                SELECT b
                FROM Board b
                JOIN b.members m
                WHERE m.userId = :userId
                  AND b.status = :status
                  AND m.active = true
                ORDER BY m.joinedAt DESC
            """)
    List<Board> findBoardByMemberActiveTrueAndMemberUserIdAndOrderByMemberJoinedAt(
            @Param("userId") UUID userId,
            @Param("status") BoardStatus status);

    // Bloquea la fila del tablero para escritura hasta que finalice la transacción
    // (bloqueo pesimista)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Board b WHERE b.id = :boardId")
    Optional<Board> findByIdWithLock(UUID boardId);
}
