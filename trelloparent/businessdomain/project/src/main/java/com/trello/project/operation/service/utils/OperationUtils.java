package com.trello.project.operation.service.utils;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.service.OperationProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationUtils {

    private final OperationProjectService operationProjectService;

    // Método para marcar el estado de la operación como
    // MANUAL_INTERVENTION_REQUIRED
    public void markManualReview(
            Operation operation,
            Exception exception) {

        operation.setStatus(
                OperationStatus.MANUAL_INTERVENTION_REQUIRED);
        operation.setCompletedAt(null);
        operation.setNextAutomaticAttemptAt(null);

        log.error(
                "Operation {} requires manual intervention. Type: {}",
                operation.getId(),
                operation.getType(),
                exception);

        operationProjectService.saveOperation(operation);
    }

    public void markCompleted(Operation operation) {
        operation.setStatus(OperationStatus.COMPLETED);
        operation.setCompletedAt(LocalDateTime.now());
        // Borrar intento automatico
        operation.setNextAutomaticAttemptAt(null);

        operationProjectService.saveOperation(operation);
    }

    public void markCompensationPending(Operation operation) {
        operation.setStatus(OperationStatus.COMPENSATION_PENDING);
        operation.setCompletedAt(null);
        // Registrar un nuevo siguiente intento automatico
        operation.setNextAutomaticAttemptAt(
                LocalDateTime.now().plusSeconds(30));

        operationProjectService.saveOperation(operation);
    }

    public void markExecuting(Operation operation) {
        operation.setStatus(OperationStatus.EXECUTING);
        operationProjectService.saveOperation(operation);
    }

    public void markLocalCompensationInProgress(Operation operation) {
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        // operation.setCompletedAt(null);
        // operation.setNextAutomaticAttemptAt(null);
        operationProjectService.saveOperation(operation);
    }

    public void markLocalCompensationCompleted(Operation operation) {
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
        // operation.setCompletedAt(null);
        // operation.setNextAutomaticAttemptAt(
        // LocalDateTime.now().plusSeconds(30));

        operationProjectService.saveOperation(operation);
    }

    public void markCompensationCompleted(Operation operation) {
        operation.setStatus(OperationStatus.COMPENSATION_COMPLETED);
        // operation.setNextAutomaticAttemptAt(null);

        // completedAt queda reservado para la operación original.
        // operation.setCompletedAt(null);

        operationProjectService.saveOperation(operation);
    }
}