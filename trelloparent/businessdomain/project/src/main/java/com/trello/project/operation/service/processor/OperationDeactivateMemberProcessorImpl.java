package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.exception.RemoteBoardAccessNotFoundException;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Operation;
import com.trello.project.enums.CleanupStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationDeactivateMemberService;
import com.trello.project.operation.service.utils.AbstractOperationProcessor;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeactivateMemberProcessorImpl extends AbstractOperationProcessor
        implements OperationDeactivateMemberProcessor {
    private final WorkflowClientService workflowClientService;
    private final MemberProjectService memberProjectService;
    private final OperationDeactivateMemberService operationDeactivateMemberService;

    public OperationDeactivateMemberProcessorImpl(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            MemberProjectService memberProjectService,
            OperationDeactivateMemberService operationDeactivateMemberService) {

        super(operationUtils, operationProjectService);

        this.workflowClientService = workflowClientService;
        this.memberProjectService = memberProjectService;
        this.operationDeactivateMemberService = operationDeactivateMemberService;
    }

    @Override
    public void processDeactivateMemberOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, OperationCompensatedException,
            BusinessRuleException, MemberNotFoundException, OperationNotPendingException,
            OperationManualReviewException {
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DEACTIVATE_MEMBER) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);
    }

    @Override
    protected void executeOriginalOperation(Operation operation) {
        UUID memberId = operation.getMemberId();
        UUID userId = operation.getUserId();
        UUID boardId = operation.getBoardId();
        Role oldRole = operation.getOldRole();

        // Cambiar el rol antiguo
        WorkflowRole workflowOldRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                oldRole);

        memberProjectService.findMemberByIdAndUserId(memberId, userId);

        workflowClientService.deactivateBoardAccess(boardId, userId, workflowOldRole,
                operation.getId());
    }

    @Override
    protected void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            log.info("OPERACION NO PENDIENTE DESDE processLocalCompensation");
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationInProgress(operation);
            operationDeactivateMemberService.revertDeactivateMember(operation.getId());

            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.MANUAL_INTERVENTION_REQUIRED) {
                throw new OperationManualReviewException();
            }
            operationUtils.markLocalCompensationCompleted(updatedOperation);

        } catch (OperationManualReviewException e) {
            log.info("EXCEPTION: " + e);
        } catch (Exception exception) {
            operationUtils.markManualReview(operation, exception);

            log.info("EXCEPTION: " + exception);
            throw new OperationManualReviewException();
        }
    }

    @Override
    protected void processRemoteCompensation(Operation operation) {
        Operation updatedOperation = findOperation(operation.getId());

        if (updatedOperation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            log.info("OPERACION NO PENDIENTE DESDE processRemoteCompensation");
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markCompensationInProgress(operation);
            workflowClientService.revertDeactivateBoardAccess(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException exception) {
            operationUtils.markLocalCompensationCompletedFromServiceUnavailable(operation);
            log.info("EXCEPTION: " + exception);
            throw new OperationCompensatedException();
        } catch (RemoteBoardAccessNotFoundException exception) {
            operationUtils.markManualReview(operation, exception);
            log.info("EXCEPTION: " + exception);
            throw new OperationManualReviewException();
        }
    }

    // Método para procesar todas las operaciones que se van a cancelar por ID de
    // miembro
    @Override
    public void processCancelOperationsByMemberId(UUID memberId, UUID operationId) {
        try {

            // Indicar que se está ejecutando la limpieza remota.
            operationProjectService.updateOperationCleanupStatusByMemberIdAndStatus(
                    CleanupStatus.EXECUTING,
                    memberId,
                    OperationStatus.CANCELED);

            workflowClientService.cancelOperationsByMemberId(memberId);

            // Confirmar que la limpieza remota terminó correctamente.
            operationProjectService.updateOperationCleanupStatusByMemberIdAndStatus(
                    CleanupStatus.COMPLETED,
                    memberId,
                    OperationStatus.CANCELED);

        } catch (ServiceUnavailableException e) {

            log.warn(
                    "Unable to cancel operations associated with member {} in Workflow. Operation {} will require a retry.",
                    memberId,
                    operationId);

            // Revertir estado de operaciones en project, escribir en el campo cleanup el
            // valor PENDING

            // Vuelve al estado original, implica que no se pudo realizar el llamado en el
            // microservicio Workflow para cancelar la operacioin
            // Restaurar el estado de limpieza para permitir un nuevo intento.
            operationProjectService.updateOperationCleanupStatusByMemberIdAndStatus(
                    CleanupStatus.PENDING,
                    memberId,
                    OperationStatus.CANCELED);

            throw e;
        }
    }
}
