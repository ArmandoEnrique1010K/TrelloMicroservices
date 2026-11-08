package com.trello.project.operation.service.utils;

import java.util.UUID;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

// Implementa el patrón Template Method para centralizar el flujo general de ejecución y 
// compensación de las operaciones SAGA. Las clases concretas implementan los pasos específicos 
// de cada operación, mientras que esta clase abstracta coordina su ejecución y recuperación.
@Slf4j
public abstract class AbstractOperationProcessor {
    protected final OperationUtils operationUtils;
    protected final OperationProjectService operationProjectService;

    protected AbstractOperationProcessor(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService) {

        this.operationUtils = operationUtils;
        this.operationProjectService = operationProjectService;
    }

    // Define el algoritmo general de procesamiento de una operación SAGA,incluyendo
    // su ejecución original y las compensaciones local y remota
    protected void processOriginalOperation(Operation operation)
            throws OperationCompensatedException, OperationNotFoundException,
            BusinessRuleException {

        // 1. Marcar la operación como EXECUTING antes de ejecutar la lógica
        // de negocio y comunicarse con el microservicio Workflow.
        operationUtils.markExecuting(operation);

        try {

            // 2. Ejecutar la operación original en el microservicio Workflow
            executeOriginalOperation(operation);

            // 3. Registrar que la operación finalizó correctamente.
            operationUtils.markCompleted(operation);

        } catch (ServiceUnavailableException exception) {
            // La comunicación con Workflow no está disponible y el resultado
            // de la operación remota puede ser desconocido.

            // Iniciar la compensación para revertir los cambios locales y,
            // posteriormente, intentar compensar los cambios remotos.
            operationUtils.markCompensationPending(operation);

            try {
                // 4. Intentar compensar inmediatamente los cambios locales.
                // Si esta compensación termina, su estado permitirá continuar
                // desde la compensación remota en futuros reintentos.
                processLocalCompensation(operation);

                // 5. Intentar revertir los cambios realizados en Workflow.
                processRemoteCompensation(operation);

            } catch (ServiceUnavailableException compensationException) {
                // Si falla la comunicación durante la compensación remota,
                // consultar el estado persistido para determinar si la
                // compensación distribuida llegó a completarse.

                Operation updated = findOperation(operation.getId());

                // Si la compensación ya terminó, informar que la operación
                // original fue compensada, aunque se haya producido una
                // excepción durante el proceso de compensación.
                if (updated.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                    // Si la compensación no llegó a completarse, propagar el error
                    // de comunicación para que se gestione según el flujo previsto.
                    log.info("EXCEPTION: " + compensationException);
                    throw new OperationCompensatedException();
                }
            }

            // 6. Comprobar el estado persistido después de intentar ambas
            // compensaciones, ya que la operación original no se completó.
            Operation updated = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updated.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                log.info("EXCEPTION: " + exception);
                throw new OperationCompensatedException();
            }

            // } catch (OperationCompensatedException
            // | OperationManualReviewException e) {
            // throw e;

            // } catch (OperationManualReviewException exceptionManualReview) {
            // log.info("EXCEPTION: " + exceptionManualReview);

        } catch (Exception exception) {
            // Cualquier otro error se registra para revisión manual, dado que
            // no se ha definido aquí una estrategia segura de recuperación
            // automática para esta excepción.
            operationUtils.markManualReview(operation, exception);

            log.info("EXCEPTION: " + exception);
            throw new BusinessRuleException();
        }
    }

    // Recupera una operación persistida mediante su identificador
    protected Operation findOperation(UUID operationId)
            throws OperationNotFoundException {

        return operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
    }

    // Ejecuta la operación original en el microservicio Workflow
    protected abstract void executeOriginalOperation(Operation operation);

    // Ejecuta la compensación de los cambios locales en Project
    protected abstract void processLocalCompensation(Operation operation);

    // Ejecuta la compensación de los cambios remotos en Workflow
    protected abstract void processRemoteCompensation(Operation operation);

}
