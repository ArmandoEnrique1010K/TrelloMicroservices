package com.trello.project.operation.service.processor;

import java.time.LocalDateTime;
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
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.OperationCompensatedException;
import com.trello.project.exception.OperationManualReviewException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationAcceptInvitationService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationProcessorImpl implements OperationProcessor {
    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final InvitationProjectService invitationProjectService;

    private final OperationAcceptInvitationService operationAcceptInvitationService;

    public OperationProcessorImpl(OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            InvitationProjectService invitationProjectService,
            OperationAcceptInvitationService operationAcceptInvitationService) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.invitationProjectService = invitationProjectService;
        this.operationAcceptInvitationService = operationAcceptInvitationService;
    }

    // No se coloca un @Transactional porque este método se encarga de hacer un
    // llamado al cliente
    @Override
    public void processOperation(UUID operationId) {
        // 1. Recuperar la operación persistida en Project.
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        // 2. Determinar qué proceso corresponde ejecutar.
        OperationStatus currentStatus = operation.getStatus();

        // Solamente se acepta con estado PENDING

        // POSIBLES ESTADOS DE OPERACIONES:
        // EXECUTION_PENDING -> Si se trata de una nueva operación
        //

        // Evitar reprocesar operaciones ya completadas.
        switch (currentStatus) {
            // La operación original todavía no se ha ejecutado.
            case EXECUTION_PENDING ->
                processOriginalOperation(operation);

            // El rollback local de Project está pendiente.
            // case COMPENSATION_PENDING ->
            // processLocalCompensation(operation);

            // El rollback local ya terminó.
            // Solo se debe reintentar la compensación remota.

            // Esto no va a funcionar, debe volver a procesar la operación original
            // processRemoteCompensation(operation)

            // Probar si funciona el caso de cuando el usuario vuelve a llamar al metodo del
            // endpoint para realizar la misma operación que ya fue realizada y obtener un
            // status 202 mediante la excepción OperationCompensatedException

            // case COMPENSATION_COMPLETED ->
            // processOriginalOperation(operation);

            // No ejecutar nuevamente operaciones terminadas
            // ni operaciones que requieren revisión manual.
            default -> throw new OperationNotPendingException();
        }
    }

    // Procesar la operación original
    private void processOriginalOperation(Operation operation) {
        log.info("ESTADO ACTUAL: " + operation.getStatus());

        // 1. Marcar el inicio de la ejecución original.
        operation.setStatus(OperationStatus.EXECUTING);
        operationProjectService.saveOperation(operation);

        try {

            // 2. Ejecutar la operación original según su tipo.
            switch (operation.getType()) {

                case ACCEPT_INVITATION ->
                    processAcceptInvitation(operation);

                default -> throw new IllegalArgumentException(
                        "Unsupported operation type: " + operation.getType());
            }

            // 3. La operación original terminó correctamente.
            operation.setStatus(OperationStatus.COMPLETED);
            operation.setCompletedAt(LocalDateTime.now());

            // Borrar intento automatico
            operation.setNextAutomaticAttemptAt(null);

            // En este caso todo salio bien y no se lanzara ninguna excepción
            operationProjectService.saveOperation(operation);

        } catch (ServiceUnavailableException e) {
            // En este caso como la operación original falló, se inicia el proceso de
            // compensación (revertir los cambios realizados en la base de datos)

            // 4. El resultado remoto puede ser desconocido.
            // Iniciar el proceso de compensación.
            operation.setStatus(OperationStatus.COMPENSATION_PENDING);
            operation.setCompletedAt(null);

            // Registrar un nuevo siguiente intento automatico
            operation.setNextAutomaticAttemptAt(
                    LocalDateTime.now().plusSeconds(30));

            operationProjectService.saveOperation(operation);

            // Intentar compensar inmediatamente.
            // Si falla, el estado y la fecha permiten recuperarlo después.
            // 2. Intentar compensar inmediatamente.
            try {
                processLocalCompensation(operation);

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
            markManualReview(operation, e);

            throw new OperationManualReviewException();

        } catch (Exception e) {

            // 6. No asumir que cualquier error es un error de negocio.
            // Registrar el error técnico y definir una política de recuperación.

            // Por el momento se registra como un MANUAL_REVIEW, requiere de intervención en
            // la base de datos
            markManualReview(operation, e);

            throw new BusinessRuleException();
        }
    }

    // Ejecutar la compensación Local
    private void processLocalCompensation(Operation operation) {

        // Posible estado de operacion: COMPENSATION_PENDING

        // 1. Confirmar que corresponde compensar.
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {

            // 3. Revertir los cambios locales de Project.
            // La compensación local debe ejecutarse en una transacción
            // independiente y terminar antes de llamar a Workflow.
            operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
            operation.setCompletedAt(null);
            operation.setNextAutomaticAttemptAt(null);
            operationProjectService.saveOperation(operation);

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
            operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
            operation.setCompletedAt(null);
            operation.setNextAutomaticAttemptAt(
                    LocalDateTime.now().plusSeconds(30));

            operationProjectService.saveOperation(operation);

        } catch (Exception e) {

            log.error(
                    "Error en la compensación local de la operación {}",
                    operation.getId(),
                    e);

            // Una excepción inesperada durante el rollback local
            // no debe considerarse automáticamente recuperable.

            markManualReview(operation, e);
            throw new OperationManualReviewException();

        }

        // La compensación local ya terminó.
        // Si falla Workflow, el siguiente intento no repetirá el rollback.
        processRemoteCompensation(operation);
    }

    private void processRemoteCompensation(Operation operation) {
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

                markManualReview(
                        operation,
                        new IllegalStateException(
                                "Estado previo incompatible con la compensación"));

                throw new OperationManualReviewException();
            }

            // La compensación distribuida terminó.
            operation.setStatus(OperationStatus.COMPENSATION_COMPLETED);
            operation.setNextAutomaticAttemptAt(null);

            // completedAt queda reservado para la operación original.
            operation.setCompletedAt(null);

            operationProjectService.saveOperation(operation);

        } catch (ServiceUnavailableException e) {

            // Los cambios locales ya fueron revertidos.
            // No regresar a COMPENSATION_PENDING: eso repetiría el rollback.
            operation.setStatus(OperationStatus.LOCAL_COMPENSATION_COMPLETED);
            operation.setNextAutomaticAttemptAt(
                    LocalDateTime.now().plusSeconds(30));

            operationProjectService.saveOperation(operation);

            throw new OperationCompensatedException();
        }
    }

    //

    // Método para llamar a los respectivos clientes para ejecutar la operación de
    // aceptar invitacion en el microservicio Workflow
    private void processAcceptInvitation(Operation operation) {

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
            log.info(
                    "Ejecutando addBoardAccess. operationId={}, boardId={}",
                    operation.getId(),
                    boardId);

            // Si no existia un miembro, crear el permiso de acceso en Workflow
            workflowClientService.addBoardAccess(
                    boardId,
                    workflowRole,
                    operation.getId());
            log.info(
                    "addBoardAccess finalizó correctamente. operationId={}",
                    operation.getId());

        } else if (!memberWasActive) {
            log.info(
                    "Ejecutando activateBoardAccess. operationId={}, boardId={}",
                    operation.getId(),
                    boardId);

            // Si existia un miembro inactivo, entonces activar el permiso de acceso en
            // Workflow.
            log.info("DATOS OBTENIDOS: BoardId: " + boardId + " | UserId: " + operation.getUserId() + " | Role: "
                    + workflowRole
                    + " | OperationId: " + operation.getId());

            workflowClientService.activateBoardAccess(
                    boardId,
                    operation.getUserId(),
                    workflowRole,
                    operation.getId());

            log.info(
                    "activateBoardAccess finalizó correctamente. operationId={}",
                    operation.getId());

        } else {
            // Si existia un miembro activo, entonces devolver una excepción
            throw new BusinessRuleException();
        }
    }

    // Método privado para marcar el estado de la operación como MANUAL_REVIEW
    private void markManualReview(
            Operation operation,
            Exception exception) {

        log.error(
                "La operación {} requiere revisión manual: ",
                operation.getId(),
                "Excepción: ",
                exception);

        operation.setStatus(OperationStatus.MANUAL_INTERVENTION_REQUIRED);
        operation.setCompletedAt(null);
        operation.setNextAutomaticAttemptAt(null);

        operationProjectService.saveOperation(operation);
    }

}
