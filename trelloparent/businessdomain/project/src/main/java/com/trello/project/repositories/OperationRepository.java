package com.trello.project.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.project.entities.Operation;
import com.trello.project.enums.CleanupStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar y tomar la primera operación por ID de invitación y tipo de operación,
    // de una lista de operaciones ordenadas de forma descendente por fecha de
    // creación
    Optional<Operation> findFirstByInvitationIdAndTypeOrderByCreatedAtDesc(UUID invitationId, OperationType type);

    Optional<Operation> findFirstByMemberIdAndTypeOrderByCreatedAtDesc(UUID memberId, OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndOwnerUserIdAndBoardNameAndTypeOrderByCreatedAtDesc(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndBoardNameAndTypeOrderByCreatedAtDesc(
            UUID workspaceId,
            String boardName,
            OperationType type);

    Optional<Operation> findFirstByBoardIdAndTypeOrderByCreatedAtDesc(UUID boardId, OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndTypeOrderByCreatedAtDesc(UUID workspaceId, OperationType type);

    // Listar operaciones que tengan un estado mencionado en la lista y ordenarlas
    // de forma ascendente (menor a mayor) por fecha de creación e ID

    List<Operation> findByStatusInOrderByWorkspaceIdAscCreatedAtAscIdAsc(
            List<OperationStatus> statuses);

    List<Operation> findByTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
            OperationType operationType,
            OperationStatus operationStatus);

    // Comprueba si existe alguna operación del mismo Workspace cuya fecha de
    // creación sea anterior a la indicada y cuyo estado no esté incluido en la
    // lista de estados completados.
    boolean existsByWorkspaceIdAndCreatedAtLessThanAndStatusNotIn(
            UUID workspaceId,
            LocalDateTime createdAt,
            List<OperationStatus> completedStatuses);

    // No olvidar la anotación @Modifying para que pueda ejecutar la operación de
    // borrado fisico en la base de datos
    @Modifying
    void deleteAllByStatus(OperationStatus status);

    @Modifying
    void deleteAllByBoardId(UUID boardId);

    boolean existsByTypeAndWorkspaceIdAndBoardName(OperationType operationType, UUID workspaceId, String boardName);

    boolean existsByTypeAndInvitationId(OperationType operationType, UUID invitationId);

    @Modifying
    @Query("""
                UPDATE Operation o
                SET o.status = :status,
                    o.cleanupStatus = :cleanupStatus
                WHERE o.boardId = :boardId
                AND o.id <> :excludedOperationId
                AND o.status IN :allowedStatuses
            """)
    void cancelAllByBoardId(
            UUID boardId,
            UUID excludedOperationId,
            List<OperationStatus> allowedStatuses,
            OperationStatus status,
            CleanupStatus cleanupStatus);

    @Modifying
    @Query("""
                UPDATE Operation o
                SET o.status = :status,
                    o.cleanupStatus = :cleanupStatus
                WHERE o.workspaceId = :workspaceId
                AND o.id <> :excludedOperationId
                AND o.status IN :allowedStatuses
            """)
    void cancelAllByWorkspaceId(
            UUID workspaceId,
            UUID excludedOperationId,
            List<OperationStatus> allowedStatuses,
            OperationStatus status,
            CleanupStatus cleanupStatus);

    @Modifying
    @Query("""
                UPDATE Operation o
                SET o.status = :status,
                    o.cleanupStatus = :cleanupStatus
                WHERE o.memberId = :memberId
                AND o.id <> :excludedOperationId
                AND o.status IN :allowedStatuses
            """)
    void cancelAllByMemberId(
            UUID memberId,
            UUID excludedOperationId,
            List<OperationStatus> allowedStatuses,
            OperationStatus status,
            CleanupStatus cleanupStatus);

    // Actualizar operacion, cambiar el estado de cleanupStatus por boardId y status
    @Modifying
    @Query("""
            UPDATE Operation o
            SET o.cleanupStatus = :cleanupStatus
            WHERE o.boardId = :boardId
            AND o.status = :operationStatus
            """)
    void updateCleanupStatusByBoardIdAndStatus(
            UUID boardId,
            OperationStatus operationStatus,
            CleanupStatus cleanupStatus);

    @Modifying
    @Query("""
            UPDATE Operation o
            SET o.cleanupStatus = :cleanupStatus
            WHERE o.workspaceId = :workspaceId
            AND o.status = :operationStatus
            """)
    void updateCleanupStatusByWorkspaceIdAndStatus(
            UUID workspaceId,
            OperationStatus operationStatus,
            CleanupStatus cleanupStatus);

    @Modifying
    @Query("""
            UPDATE Operation o
            SET o.cleanupStatus = :cleanupStatus
            WHERE o.memberId = :memberId
            AND o.status = :operationStatus
            """)
    void updateCleanupStatusByMemberIdAndStatus(
            UUID memberId,
            OperationStatus operationStatus,
            CleanupStatus cleanupStatus);

    // Actualiza el estado únicamente si la operación conserva el estado esperado.
    // La actualización condicional es atómica y evita que dos ejecutores
    // reclamen simultáneamente la misma operación.
    @Modifying
    @Query("""
                UPDATE Operation o
                SET o.status = :newStatus
                WHERE o.id = :operationId
                AND o.status = :expectedStatus
            """)
    int updateStatusIfCurrent(
            @Param("operationId") UUID operationId,
            @Param("expectedStatus") OperationStatus expectedStatus,
            @Param("newStatus") OperationStatus newStatus);

    @Modifying
    @Query("""
                UPDATE Operation o
                SET o.status = :newStatus,
                    o.completedAt = :completedAt
                WHERE o.id = :operationId
                AND o.status = :expectedStatus
            """)
    int updateStatusAndCompletedAt(
            @Param("operationId") UUID operationId,
            @Param("expectedStatus") OperationStatus expectedStatus,
            @Param("newStatus") OperationStatus newStatus,
            @Param("completedAt") LocalDateTime completedAt);

    @Modifying
    void deleteAllByStatusAndCleanupStatus(OperationStatus status, CleanupStatus cleanupStatus);

}
