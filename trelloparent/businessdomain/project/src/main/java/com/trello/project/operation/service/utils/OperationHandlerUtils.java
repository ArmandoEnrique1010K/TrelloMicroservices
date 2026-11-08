package com.trello.project.operation.service.utils;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.OperationProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationHandlerUtils {
    private final OperationProjectService operationProjectService;

    // Método para preparar la ejecución de una operación
    @Transactional(rollbackFor = Exception.class)
    public Operation prepareExecution(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException {

        // 1. Recuperar la operación registrada previamente para identificar
        // el proceso de aceptación que se va a ejecutar.
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // 2. Validar que la operación se encuentre en un estado que permita
        // iniciar un nuevo intento de ejecución.
        OperationStatus status = operation.getStatus();

        if (status != OperationStatus.REGISTERED
                && status != OperationStatus.LOCAL_COMPENSATION_COMPLETED
                && status != OperationStatus.COMPENSATION_COMPLETED) {

            throw new OperationNotPendingException();
        }

        // 3. Incrementar el contador de intentos según el origen
        // de la ejecución: manual o automática.
        if (automatic) {
            operation.setAutomaticAttempts(operation.getAutomaticAttempts() + 1);
        } else {
            operation.setManualAttempts(operation.getManualAttempts() + 1);
        }

        // 4. Registrar la operación como pendiente de ejecución y actualizar
        // los datos utilizados para controlar su procesamiento y reintentos.
        operation.setStatus(OperationStatus.EXECUTION_PENDING);
        operation.setCompletedAt(null);
        operation.setNextAutomaticAttemptAt(
                LocalDateTime.now().plusSeconds(30));

        // 5. Persistir los cambios de estado de la operación junto
        // con los datos de ejecución actualizados.
        return operationProjectService.saveOperation(operation);
    }
}
