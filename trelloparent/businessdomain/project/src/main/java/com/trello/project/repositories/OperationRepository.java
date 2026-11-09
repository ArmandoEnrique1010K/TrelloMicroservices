package com.trello.project.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.trello.project.entities.Operation;
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

    Optional<Operation> findFirstByWorkspaceIdAndIdempotencyKeyAndTypeOrderByCreatedAtDesc(UUID workspaceId,
            UUID idempotencyKey, OperationType type);

    Optional<Operation> findFirstByBoardIdAndTypeOrderByCreatedAtDesc(UUID boardId, OperationType type);

    Optional<Operation> findFirstByWorkspaceIdAndTypeOrderByCreatedAtDesc(UUID workspaceId, OperationType type);

    // Listar operaciones que tengan un estado mencionado en la lista y ordenarlas
    // de forma ascendente (menor a mayor) por fecha de siguiente intento automatico
    // Y que el valor "nextAutomaticAttemptAtAsc" sea menor que la fecha actual

    // Y tambien por ID de espacio de trabajo, dado que los 6 tipos de operaciones
    // guardan el ID de espacio de trabajo
    List<Operation> findByStatusInAndNextAutomaticAttemptAtLessThanOrderByNextAutomaticAttemptAtAscWorkspaceIdAsc(
            List<OperationStatus> statuses,
            LocalDateTime currentDate);

    // Comprueba si existe alguna operación del mismo Workspace cuya fecha de
    // creación sea anterior a la indicada y cuyo estado no esté incluido en la
    // lista de estados completados.
    boolean existsByWorkspaceIdAndCreatedAtLessThanAndStatusNotIn(
            UUID workspaceId,
            LocalDateTime createdAt,
            List<OperationStatus> completedStatuses);

    // Listar operaciones por una lista de tipos y estado de operacion, ordenados de
    // forma ascendente por fecha de creación
    // List<Operation> findByTypeInAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
    // List<OperationType> operationTypes,
    // OperationStatus operationStatus);

    List<Operation> findByTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
            OperationType operationType,
            OperationStatus operationStatus);

    List<Operation> findByType(OperationStatus operationStatus);

    // No olvidar la anotación para que pueda ejecutar la operación de borrado
    // fisico
    @Modifying
    void deleteAllByStatus(OperationStatus status);

    boolean existsByTypeAndWorkspaceIdAndBoardName(OperationType operationType, UUID workspaceId, String boardName);

    // Borrar operaciones completadas
    // void deleteByBoardIdAndRecipientUserIdAndSenderUserId(
    // UUID boardId,
    // UUID recipientUserId,
    // UUID senderUserId);

    // @Modifying
    // @Query("""
    // DELETE FROM Invitation i
    // WHERE i.acceptedAt <= :now
    // """)
    // void deleteAcceptedInvitations(@Param("now") LocalDateTime now);

}
