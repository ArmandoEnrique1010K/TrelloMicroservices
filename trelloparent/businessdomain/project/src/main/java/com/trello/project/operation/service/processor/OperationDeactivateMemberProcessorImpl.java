package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationDeactivateMemberService;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationDeactivateMemberProcessorImpl implements OperationDeactivateMemberProcessor {

    private final OperationProjectService operationProjectService;
    private final WorkflowClientService workflowClientService;
    private final OperationUtils operationUtils;
    private final MemberProjectService memberProjectService;
    private final OperationDeactivateMemberService operationDeactivateMemberService;

    public OperationDeactivateMemberProcessorImpl(
            OperationProjectService operationProjectService,
            WorkflowClientService workflowClientService,
            OperationUtils operationUtils,
            MemberProjectService memberProjectService,
            OperationDeactivateMemberService operationDeactivateMemberService) {
        this.operationProjectService = operationProjectService;
        this.workflowClientService = workflowClientService;
        this.operationUtils = operationUtils;
        this.memberProjectService = memberProjectService;
        this.operationDeactivateMemberService = operationDeactivateMemberService;
    }

    @Override
    public void processDeactivateMemberOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException {
        Operation operation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        if (operation.getType() != OperationType.DEACTIVATE_MEMBER) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);
    }

    private void processOriginalOperation(Operation operation) {
        operationUtils.markExecuting(operation);

        try {
            processDeactivateMember(operation);
            operationUtils.markCompleted(operation);

        } catch (ServiceUnavailableException e) {
            operationUtils.markCompensationPending(operation);

            try {
                processLocalCompensation(operation);
                processRemoteCompensation(operation);

            } catch (ServiceUnavailableException compensationException) {
                Operation updatedOperation = operationProjectService
                        .findOperationById(operation.getId())
                        .orElseThrow(OperationNotFoundException::new);

                if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                    throw new OperationCompensatedException();
                }

                throw compensationException;
            }

            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.COMPENSATION_COMPLETED) {
                throw new OperationCompensatedException();
            }

            // TODO: DEFINIR UN BLOQUE CATCH CON LAS EXCEPCIONES DE TIPO RUNTIME EXCEPTION
            // DESDE EL MICROSERVICIO WORKFLOW
        } catch (Exception e) {
            operationUtils.markManualReview(operation, e);
            throw new BusinessRuleException();
        }
    }

    private void processDeactivateMember(Operation operation) {

        UUID memberId = operation.getMemberId();
        UUID userId = operation.getUserId();
        UUID boardId = operation.getBoardId();
        Role oldRole = operation.getOldRole();

        // Cambiar el rol antiguo
        WorkflowRole workflowOldRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                oldRole);

        // Verificar que el miembro exista
        log.info("id de miembro: " + memberId);
        log.info("id de usuario: " + userId);

        memberProjectService.findMemberByIdAndUserId(memberId, userId);

        workflowClientService.deactivateBoardAccess(boardId, userId, workflowOldRole,
                operation.getId());
    }

    private void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationCompleted(operation);
            operationDeactivateMemberService.revertDeactivateMember(operation.getId());

            Operation updatedOperation = operationProjectService
                    .findOperationById(operation.getId())
                    .orElseThrow(OperationNotFoundException::new);

            if (updatedOperation.getStatus() == OperationStatus.MANUAL_INTERVENTION_REQUIRED) {
                throw new OperationManualReviewException();
            }
            operationUtils.markLocalCompensationCompleted(updatedOperation);

        } catch (OperationManualReviewException e) {
            throw e;
        } catch (Exception e) {
            operationUtils.markManualReview(operation, e);
            throw new OperationManualReviewException();
        }
    }

    private void processRemoteCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {
            workflowClientService.revertDeactivateBoardAccess(operation.getId());
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }

}
