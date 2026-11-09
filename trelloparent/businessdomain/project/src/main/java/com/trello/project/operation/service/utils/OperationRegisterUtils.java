package com.trello.project.operation.service.utils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.operation.exception.OperationNotPendingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationRegisterUtils {

    public Operation findReusableOperation(
            Optional<Operation> optionalOperation) {
        if (optionalOperation.isEmpty()) {
            return null;
        }

        Operation operation = optionalOperation.get();

        switch (operation.getStatus()) {
            case REGISTERED,
                    LOCAL_COMPENSATION_COMPLETED,
                    COMPENSATION_COMPLETED -> {
                // Reutilizar la operación existente para reintentar el proceso
                // conservando su identificador.

                //

                // De acuerdo al tipo de operación debe actualizar unos campos
                // desde el servicio encargado del registro de operaciones

                // ACCEPT_INVITATION / CHANGE_ROLE_MEMBER
                // Cambiar el valor de "newRole"

                // CREATE_BOARD
                // Cambiar los valores de "boardName" y "boardDescription"

                // DEACTIVATE_MEMBER / DELETE_BOARD / DELETE_WORKSPACE
                // No cambiar ningun campo

                //

                // Devolver la operación existente para continuar el flujo
                // utilizando el mismo identificador.
                return operation;
            }

            // La operación se encuentra en ejecución, pendiente de ejecución
            // o compensación, o requiere intervención manual. No se permite
            // iniciar otro intento mientras esta operación no se resuelva.
            case EXECUTION_PENDING,
                    EXECUTING,
                    COMPENSATION_PENDING,
                    LOCAL_COMPENSATION_IN_PROGRESS,
                    MANUAL_INTERVENTION_REQUIRED ->

                throw new OperationNotPendingException();

            // La operación anterior finalizó correctamente. No se reutiliza;
            // se continúa con el flujo para registrar una nueva operación
            // de aceptación de la invitación.
            case COMPLETED, LOGICAL_DELETE_COMPLETED -> {
                return null;
            }
        }

        return null;
    }

    // Crea la operación, pero no lo guarda porque se requiere actualizar otros
    // campos desde el servicio de registro de operaciones
    public Operation createOperation(OperationType type) {
        Operation operation = new Operation();

        // Generar el identificador que permitirá relacionar los pasos
        // posteriores de la operación.
        UUID operationId = UUID.randomUUID();

        operation.setId(operationId);
        operation.setType(type);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Registrar la fecha de creación y dejar sin programar el siguiente
        // intento automático hasta que corresponda según el flujo.
        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        return operation;
    }
}
