package com.trello.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.CleanupStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.repositories.OperationRepository;

@Service
public class OperationProjectServiceImpl implements OperationProjectService {

    private final OperationRepository operationRepository;

    public OperationProjectServiceImpl(
            OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Optional<Operation> findOperationById(UUID operationId) {
        Optional<Operation> operation = operationRepository.findById(operationId);
        return operation;
    }

    @Override
    public Operation saveOperation(Operation operation) {
        return operationRepository.save(operation);
    }

    @Override
    public Optional<Operation> findLatestOperationByInvitationIdAndType(UUID invitationId, OperationType type) {
        return operationRepository.findFirstByInvitationIdAndTypeOrderByCreatedAtDesc(invitationId, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByMemberIdAndType(UUID memberId, OperationType type) {
        return operationRepository.findFirstByMemberIdAndTypeOrderByCreatedAtDesc(memberId, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByWorkspaceIdAndBoardNameAndType(UUID workspaceId, String name,
            OperationType type) {
        return operationRepository.findFirstByWorkspaceIdAndBoardNameAndTypeOrderByCreatedAtDesc(workspaceId,
                name, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByBoardIdAndType(UUID boardId, OperationType type) {
        return operationRepository.findFirstByBoardIdAndTypeOrderByCreatedAtDesc(boardId, type);
    }

    @Override
    public Optional<Operation> findLatestOperationByWorkspaceIdAndType(UUID workspaceId, OperationType type) {
        return operationRepository.findFirstByWorkspaceIdAndTypeOrderByCreatedAtDesc(workspaceId, type);
    }

    @Override
    public boolean existsPreviousOperationsByWorkspaceIdAndCreatedAt(UUID workspaceId, LocalDateTime createdAt) {
        // Estados que permiten continuar con las operaciones posteriores:
        // - COMPLETED: la operación original terminó correctamente.
        // - LOGICAL_DELETE_COMPLETED: la operación terminó, pero aun no se borrado
        // fisicamente los datos.
        // - COMPENSATION_COMPLETED: los cambios fueron revertidos en ambos
        // microservicios.
        // - CANCELED: la operacion fue cancelada

        // LOCAL_COMPENSATION_COMPLETED no se incluye porque, aunque Project ya revirtió
        // sus cambios, todavía falta completar la compensación en Workflow.
        List<OperationStatus> completedStatuses = List.of(
                OperationStatus.COMPLETED,
                OperationStatus.LOGICAL_DELETE_COMPLETED,
                // OperationStatus.LOCAL_COMPENSATION_COMPLETED,
                OperationStatus.COMPENSATION_COMPLETED,
                OperationStatus.CANCELED);

        return operationRepository.existsByWorkspaceIdAndCreatedAtLessThanAndStatusNotIn(workspaceId, createdAt,
                completedStatuses);
    }

    @Override
    public List<Operation> findOperationsByOperationTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
            OperationType operationType, OperationStatus operationStatus) {
        return operationRepository.findByTypeAndStatusOrderByCompletedAtAscWorkspaceIdAsc(
                operationType,
                operationStatus);
    }

    @Override
    public void deleteAllOperationsByStatus(OperationStatus operationStatus) {
        operationRepository.deleteAllByStatus(operationStatus);
    }

    @Override
    public void deleteAllOperationsByStatusAndCleanupStatus(OperationStatus operationStatus,
            CleanupStatus cleanupStatus) {
        operationRepository.deleteAllByStatusAndCleanupStatus(operationStatus, cleanupStatus);
    }

    @Override
    public boolean existsOperationByTypeAndWorkspaceIdAndBoardName(OperationType operationType, UUID workspaceId,
            String boardName) {
        return operationRepository.existsByTypeAndWorkspaceIdAndBoardName(operationType, workspaceId, boardName);
    }

    @Override
    public boolean existsOperationByTypeAndInvitationId(OperationType operationType, UUID invitationId) {
        return operationRepository.existsByTypeAndInvitationId(operationType, invitationId);
    }

    @Override
    public List<Operation> findOperationsByStatusesListOrderByWorkspaceIdAscAndCreatedAtAsc(
            List<OperationStatus> statuses) {
        return operationRepository.findByStatusInOrderByWorkspaceIdAscCreatedAtAscIdAsc(statuses);
    }

    // Cancelar operaciones de forma local
    @Transactional
    @Override
    public void cancelAllOperationsByBoardId(UUID boardId, UUID excludedOperationId) {

        // No se cancelan las operaciones con estado COMPLETED,
        // MANUAL_INTERVERTION_REQUIRED, CANCELED,

        List<OperationStatus> allowedStatuses = List.of(
                OperationStatus.REGISTERED,
                OperationStatus.EXECUTION_PENDING,
                OperationStatus.EXECUTING,
                OperationStatus.COMPENSATION_PENDING,
                OperationStatus.LOCAL_COMPENSATION_COMPLETED,
                OperationStatus.COMPENSATION_COMPLETED);

        operationRepository.cancelAllByBoardId(
                boardId,
                excludedOperationId,
                allowedStatuses,
                OperationStatus.CANCELED,
                CleanupStatus.EXECUTION_PENDING);
    }

    @Transactional
    @Override
    public void cancelAllOperationsByWorkspaceId(UUID workspaceId, UUID excludedOperationId) {
        List<OperationStatus> allowedStatuses = List.of(
                OperationStatus.REGISTERED,
                OperationStatus.EXECUTION_PENDING,
                OperationStatus.EXECUTING,
                OperationStatus.COMPENSATION_PENDING,
                OperationStatus.LOCAL_COMPENSATION_COMPLETED,
                OperationStatus.COMPENSATION_COMPLETED);

        operationRepository.cancelAllByWorkspaceId(
                workspaceId,
                excludedOperationId,
                allowedStatuses,
                OperationStatus.CANCELED,
                CleanupStatus.EXECUTION_PENDING);
    }

    @Transactional
    @Override
    public void updateOperationCleanupStatusByBoardIdAndStatus(CleanupStatus cleanupStatus, UUID boardId,
            OperationStatus operationStatus) {
        operationRepository.updateCleanupStatusByBoardIdAndStatus(boardId, operationStatus, cleanupStatus);
    }

    @Transactional
    @Override
    public void updateOperationCleanupStatusByWorkspaceIdAndStatus(CleanupStatus cleanupStatus, UUID workspaceId,
            OperationStatus operationStatus) {
        operationRepository.updateCleanupStatusByWorkspaceIdAndStatus(workspaceId, operationStatus, cleanupStatus);
    }

    // Ejecuta la transición de estado en una transacción independiente,
    // garantizando que el cambio quede confirmado antes de continuar.
    // Devuelve true si la operación pudo actualizarse desde el estado esperado.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean tryMarkOperation(
            UUID operationId,
            OperationStatus expectedStatus,
            OperationStatus newStatus) {

        int updatedRows = operationRepository.updateStatusIfCurrent(
                operationId,
                expectedStatus,
                newStatus);

        return updatedRows == 1;
    }

    // Ejecuta la transición de estado y establece el campo completedAt con la fecha
    // y hora actual
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryMarkOperationAndSetCompletedAtNow(UUID operationId,
            OperationStatus expectedStatus, OperationStatus newStatus) {

        LocalDateTime currentDate = LocalDateTime.now();

        int updatedRows = operationRepository.updateStatusAndCompletedAt(
                operationId,
                expectedStatus,
                newStatus, currentDate);

        return updatedRows == 1;
    }

    // METODO PARA ESTABLECER completedAt COMO NULL
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryMarkOperationAndSetCompletedAtNull(UUID operationId,
            OperationStatus expectedStatus, OperationStatus newStatus) {

        int updatedRows = operationRepository.updateStatusAndCompletedAt(
                operationId,
                expectedStatus,
                newStatus, null);

        return updatedRows == 1;
    }

    @Override
    public void cancelAllOperationsByMemberId(UUID memberId, UUID excludedOperationId) {
        List<OperationStatus> allowedStatuses = List.of(
                OperationStatus.REGISTERED,
                OperationStatus.EXECUTION_PENDING,
                OperationStatus.EXECUTING,
                OperationStatus.COMPENSATION_PENDING,
                OperationStatus.LOCAL_COMPENSATION_COMPLETED,
                OperationStatus.COMPENSATION_COMPLETED);

        operationRepository.cancelAllByMemberId(
                memberId,
                excludedOperationId,
                allowedStatuses,
                OperationStatus.CANCELED,
                CleanupStatus.EXECUTION_PENDING);
    }

    @Override
    public void updateOperationCleanupStatusByMemberIdAndStatus(CleanupStatus cleanupStatus, UUID memberId,
            OperationStatus operationStatus) {
        operationRepository.updateCleanupStatusByMemberIdAndStatus(memberId, operationStatus, cleanupStatus);
    }
}
