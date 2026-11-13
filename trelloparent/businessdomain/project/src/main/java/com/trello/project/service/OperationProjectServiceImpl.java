package com.trello.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.CleanupStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.operation.exception.OperationNotFoundException;
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
    public Optional<Operation> findLatestOperationByWorkspaceIdAndOwnerUserIdAndBoardNameAndType(UUID workspaceId,
            UUID ownerUserId, String boardName,
            OperationType type) {
        return operationRepository.findFirstByWorkspaceIdAndOwnerUserIdAndBoardNameAndTypeOrderByCreatedAtDesc(
                workspaceId, ownerUserId, boardName, type);
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
    public List<Operation> findOperationsByStatusesListOrderByNextAutomaticAttemptAtAscWorkspaceIdAsc(
            List<OperationStatus> statuses) {
        LocalDateTime currentDate = LocalDateTime.now();

        return operationRepository
                .findByStatusInAndNextAutomaticAttemptAtLessThanOrderByNextAutomaticAttemptAtAscWorkspaceIdAsc(
                        statuses,
                        currentDate);
    }

    @Override
    public boolean existsPreviousOperationsByWorkspaceIdAndCreatedAt(UUID workspaceId, LocalDateTime createdAt) {
        // Estados que permiten continuar con las operaciones posteriores:
        // - COMPLETED: la operación original terminó correctamente.
        // - LOGICAL_DELETE_COMPLETED: la operación terminó, pero aun no se borrado
        // fisicamente los datos.
        // - COMPENSATION_COMPLETED: los cambios fueron revertidos en ambos
        // microservicios.
        //

        // TODO: ¿INCLUIR EL ESTADO DE CANCELED?
        // CANCELED: la operacion fue cancelada

        // LOCAL_COMPENSATION_COMPLETED no se incluye porque, aunque Project ya revirtió
        // sus cambios, todavía falta completar la compensación en Workflow.
        List<OperationStatus> completedStatuses = List.of(
                OperationStatus.COMPLETED,
                OperationStatus.LOGICAL_DELETE_COMPLETED,
                // OperationStatus.LOCAL_COMPENSATION_COMPLETED,
                OperationStatus.COMPENSATION_COMPLETED);

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

    // Como este metodo estara dentro de un scheduler, se coloca Transactional
    @Transactional
    @Override
    public void deleteAllOperationsByStatus(OperationStatus operationStatus) {
        operationRepository.deleteAllByStatus(operationStatus);
    }

    @Override
    public void deleteOperation(Operation operation) {
        operationRepository.delete(operation);
    }

    @Override
    public void changeStatusToCompletedOperation(UUID operationId) {
        Operation operation = operationRepository.findById(operationId).orElseThrow(
                OperationNotFoundException::new);

        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        operation.setNextAutomaticAttemptAt(null);
        operationRepository.save(operation);
    }

    @Override
    public boolean existsOperationByTypeAndWorkspaceIdAndBoardName(OperationType operationType, UUID workspaceId,
            String boardName) {
        return operationRepository.existsByTypeAndWorkspaceIdAndBoardName(operationType, workspaceId, boardName);
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
        operationRepository.cancelOperationsByBoardId(
                boardId,
                excludedOperationId,
                OperationStatus.CANCELED,
                CleanupStatus.EXECUTION_PENDING);
    }

    @Override
    public List<Operation> findOperationsByBoardIdAndStatus(UUID boardId, OperationStatus status) {
        return operationRepository.findByBoardIdAndStatus(boardId, status);
    }

    @Transactional
    @Override
    public void updateOperationCleanupStatusByBoardIdAndStatus(CleanupStatus cleanupStatus, UUID boardId,
            OperationStatus operationStatus) {
        operationRepository.updateCleanupStatusByBoardIdAndStatus(boardId, operationStatus, cleanupStatus);
    }
}
