package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.exception.RemoteBoardAccessAlreadyExistsException;
import com.trello.project.client.exception.RemoteBoardAccessNotFoundException;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationAcceptInvitationProcessorImpl implements OperationAcceptInvitationProcessor {
    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final InvitationProjectService invitationProjectService;

    private final OperationAcceptInvitationService operationAcceptInvitationService;

    private final OperationUtils operationUtils;

    public OperationAcceptInvitationProcessorImpl(OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            InvitationProjectService invitationProjectService,
            OperationAcceptInvitationService operationAcceptInvitationService,
            OperationUtils operationUtils) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.invitationProjectService = invitationProjectService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
        this.operationUtils = operationUtils;
    }

    // Método para procesar la operación de aceptar invitación
    @Override
    public void processAcceptInvitationOperation(UUID operationId) throws OperationNotFoundException,
            OperationConflictException {
        // 1. Recuperar la operación persistida en este microservicio
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.ACCEPT_INVITATION) {
            throw new OperationConflictException();
        }

        // 2. El estado de la operación siempre sera EXECUTION_PENDING
        processOriginalOperation(operation);
    }

    // Procesar la operación original
    private void processOriginalOperation(Operation operation)
            throws BusinessRuleException, OperationNotFoundException, OperationCompensatedException,
            OperationManualReviewException {
        // 1. Marcar el inicio de la ejecución original.
        operationUtils.markExecuting(operation);

        try {

            // 2. Ejecutar la operación original
            processAcceptInvitation(operation);

            // 3. La operación original terminó correctamente.
            // En este caso todo salio bien y no se lanzara ninguna excepción
            operationUtils.markCompleted(operation);

        } catch (ServiceUnavailableException e) {
            // En este caso como la operación original falló, se inicia el proceso de
            // compensación (revertir los cambios realizados en la base de datos de este
            // microservicio)

            // 4. El resultado remoto puede ser desconocido.
            // Iniciar el proceso de compensación.
            operationUtils.markCompensationPending(operation);

            // Intentar compensar inmediatamente
            // Si falla, el estado y la fecha permiten recuperarlo después.
            // 2. Intentar compensar inmediatamente.
            try {
                processLocalCompensation(operation);

                // La compensación local ya terminó.
                // Si falla el microservicio Workflow, el siguiente intento no repetirá el
                // rollback.
                processRemoteCompensation(operation);
            } catch (ServiceUnavailableException compensationException) {
                // 3. Comprobar si la compensación local terminó correctamente.
                Operation updatedOperation = operationProjectService
                        .findOperationById(operation.getId())
                        .orElseThrow(OperationNotFoundException::new);

                if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                    throw new OperationCompensatedException();
                }

                // Si no se alcanzó COMPENSATION_COMPLETED, conservar el error original.
                throw compensationException;
            }

            // 4. Si no hubo excepción, comprobar el estado final.
            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                throw new OperationCompensatedException();
            }

        } catch (RemoteBoardAccessNotFoundException
                | RemoteBoardAccessAlreadyExistsException e) {

            // AQUI <---
            // Solamente si memberActive es false quiere decir que se trata de un miembro
            // inactivo y se debe ignorar la excepción de
            // RemoteBoardAccessAlreadyExistsException
            // Boolean memberActive = operation.getMemberActive();

            // 5. Inconsistencia funcional remota.
            // Requiere inspección antes de continuar.
            operationUtils.markManualReview(operation, e);

            throw new OperationManualReviewException();

        } catch (Exception e) {

            // 6. No asumir que cualquier error es un error de negocio.
            // Registrar el error técnico y definir una política de recuperación.

            // Por el momento se registra como un MANUAL_REVIEW, requiere de intervención en
            // la base de datos
            operationUtils.markManualReview(operation, e);
            // markManualReview(operation, e);

            throw new BusinessRuleException();
        }
    }

    // Método para llamar a los respectivos clientes para ejecutar la operación de
    // aceptar invitacion en el microservicio Workflow
    private void processAcceptInvitation(Operation operation) throws BusinessRuleException {

        // 1. Recuperar la invitación para obtener el tablero y el rol.
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        operation.getInvitationId(),
                        operation.getUserId());

        UUID boardId = invitation.getBoard().getId();

        // 2. Debe hacer una conversión entre los enums de WorkflowRole y Role
        WorkflowRole workflowRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                invitation.getRole());

        Boolean memberWasActive = operation.getMemberActive();

        if (memberWasActive == null) {
            // Si no existia un miembro, crear el permiso de acceso en Workflow
            workflowClientService.addBoardAccess(
                    boardId,
                    workflowRole,
                    operation.getId());

        } else if (!memberWasActive) {
            // Si existia un miembro inactivo, entonces activar el permiso de acceso en
            // Workflow.
            workflowClientService.activateBoardAccess(
                    boardId,
                    operation.getUserId(),
                    workflowRole,
                    operation.getId());
        } else {
            // Si existia un miembro activo, entonces devolver una excepción
            throw new BusinessRuleException();
        }
    }

    // Ejecutar la compensación Local
    private void processLocalCompensation(Operation operation) throws OperationNotPendingException,
            OperationNotFoundException, OperationManualReviewException {

        // Posible estado de operacion: COMPENSATION_PENDING

        // 1. Confirmar que corresponde compensar.
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {

            // 3. Revertir los cambios locales de Project.
            // La compensación local debe ejecutarse en una transacción
            // independiente y terminar antes de llamar a Workflow.
            operationUtils.markLocalCompensationInProgress(operation);

            operationAcceptInvitationService
                    .revertAcceptInvitation(operation.getId());

            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.MANUAL_INTERVENTION_REQUIRED) {
                throw new OperationManualReviewException();
            }

            // 4. La compensación local terminó.
            // A partir de ahora, los reintentos deben continuar en Workflow.
            operationUtils.markLocalCompensationCompleted(operation);
        } catch (Exception e) {

            // Una excepción inesperada durante el rollback local
            // no debe considerarse automáticamente recuperable.
            operationUtils.markManualReview(operation, e);
            throw new OperationManualReviewException();
        }

    }

    private void processRemoteCompensation(Operation operation) throws OperationNotPendingException,
            OperationManualReviewException, OperationCompensatedException {
        // POSIBLE ESTADO DE OPERACIÓN: LOCAL_COMPENSATION_COMPLETED

        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {

            if (operation.getMemberActive() == null) {

                // El miembro no existía antes de la aceptación.
                workflowClientService.revertAddBoardAccess(
                        operation.getId());

            } else if (Boolean.FALSE.equals(
                    operation.getMemberActive())) {

                // El miembro existía, pero estaba inactivo.
                workflowClientService.revertActivateBoardAccess(
                        operation.getId());

            } else {

                operationUtils
                        .markManualReview(
                                operation,
                                new IllegalStateException(
                                        "Estado previo incompatible con la compensación"));

                throw new OperationManualReviewException();
            }

            // La compensación distribuida terminó.
            operationUtils.markCompensationCompleted(operation);

        } catch (ServiceUnavailableException e) {

            // Los cambios locales ya fueron revertidos.
            // No regresar a COMPENSATION_PENDING: eso repetiría el rollback.
            // operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
            // // operation.setNextAutomaticAttemptAt(
            // // LocalDateTime.now().plusSeconds(30));

            // operationProjectService.saveOperation(operation);
            operationUtils.markLocalCompensationCompleted(operation);

            throw new OperationCompensatedException();
        }
    }

}
