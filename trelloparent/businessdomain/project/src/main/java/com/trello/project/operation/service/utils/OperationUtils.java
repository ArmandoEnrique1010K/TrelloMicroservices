package com.trello.project.operation.service.utils;

import org.springframework.stereotype.Component;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.operation.exception.OperationNotPendingException;
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

    // public void markCanceled(Operation operation) {
    // operation.setStatus(OperationStatus.CANCELED);
    // operation.setCompletedAt(null);
    // operation.setNextAutomaticAttemptAt(null);

    // operationProjectService.saveOperation(operation);
    // }

    public void markExecuting(Operation operation) {
        // operation.setStatus(OperationStatus.EXECUTING);
        // operationProjectService.saveOperation(operation);

        // En este momento:
        // operation.getStatus() == EXECUTION_PENDING

        // En resumen la operación debe tener el estado de EXECUTION_PENDING para que
        // pueda pasar al estado EXECUTING
        boolean updated = operationProjectService.tryMarkOperation(
                operation.getId(),
                OperationStatus.EXECUTION_PENDING,
                OperationStatus.EXECUTING);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markExecuting");
            throw new OperationNotPendingException();
        }

        // La base de datos ya tiene EXECUTING.

        // Sincronizar el objeto en memoria
        operation.setStatus(OperationStatus.EXECUTING);
    }

    public void markCompleted(Operation operation) {
        // operation.setStatus(OperationStatus.COMPLETED);
        // operation.setCompletedAt(LocalDateTime.now());
        // Borrar intento automatico
        // operation.setNextAutomaticAttemptAt(null);

        // operationProjectService.saveOperation(operation);

        boolean updated = operationProjectService.tryMarkOperationAndSetCompletedAtNowAndClearNextAutomaticAttemptAt(
                operation.getId(),
                OperationStatus.EXECUTING,
                OperationStatus.COMPLETED);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markCompleted");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.COMPLETED);
    }

    public void markCompensationPending(Operation operation) {
        // operation.setStatus(OperationStatus.COMPENSATION_PENDING);
        // operation.setCompletedAt(null);
        // operation.setNextAutomaticAttemptAt(
        // LocalDateTime.now().plusSeconds(30));

        // operationProjectService.saveOperation(operation);
        boolean updated = operationProjectService
                .tryMarkOperationAndSetCompletedAtNullAndNextAutomaticAttemptAtIn30Seconds(
                        operation.getId(),
                        OperationStatus.EXECUTING,
                        OperationStatus.COMPENSATION_PENDING);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markCompensationPending");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.COMPENSATION_PENDING);
    }

    public void markLocalCompensationInProgress(Operation operation) {
        // operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        // operationProjectService.saveOperation(operation);

        boolean updated = operationProjectService.tryMarkOperation(
                operation.getId(),
                OperationStatus.COMPENSATION_PENDING,
                OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markLocalCompensationInProgress");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
    }

    public void markLocalCompensationCompleted(Operation operation) {
        // operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
        // operationProjectService.saveOperation(operation);
        boolean updated = operationProjectService.tryMarkOperation(
                operation.getId(),
                OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS,
                OperationStatus.LOCAL_COMPENSATION_COMPLETED);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markLocalCompensationCompleted");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);

    }

    public void markCompensationInProgress(Operation operation) {
        // operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
        // operationProjectService.saveOperation(operation);
        boolean updated = operationProjectService.tryMarkOperation(
                operation.getId(),
                OperationStatus.LOCAL_COMPENSATION_COMPLETED,
                OperationStatus.COMPENSATION_IN_PROGRESS);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markCompensationInProgress");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.COMPENSATION_IN_PROGRESS);

    }

    public void markCompensationCompleted(Operation operation) {
        boolean updated = operationProjectService.tryMarkOperation(
                operation.getId(),
                OperationStatus.COMPENSATION_IN_PROGRESS,
                OperationStatus.COMPENSATION_COMPLETED);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markCompensationCompleted");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.COMPENSATION_COMPLETED);
    }

    public void markLocalCompensationCompletedFromServiceUnavailable(Operation operation) {
        boolean updated = operationProjectService.tryMarkOperation(
                operation.getId(),
                OperationStatus.COMPENSATION_IN_PROGRESS,
                OperationStatus.LOCAL_COMPENSATION_COMPLETED);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markLocalCompensationCompletedFromServiceUnavailable");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
    }

    public void markLogicalDeleteCompleted(Operation operation) {
        // operation.setStatus(OperationStatus.LOGICAL_DELETE_COMPLETED);
        // operation.setCompletedAt(LocalDateTime.now());
        // operation.setNextAutomaticAttemptAt(null);

        // operationProjectService.saveOperation(operation);

        boolean updated = operationProjectService.tryMarkOperationAndSetCompletedAtNowAndClearNextAutomaticAttemptAt(
                operation.getId(),
                OperationStatus.EXECUTING,
                OperationStatus.LOGICAL_DELETE_COMPLETED);

        if (!updated) {
            log.info("OPERACION NO PENDIENTE DESDE markLogicalDeleteCompleted");
            throw new OperationNotPendingException();
        }

        operation.setStatus(OperationStatus.LOGICAL_DELETE_COMPLETED);

    }

}