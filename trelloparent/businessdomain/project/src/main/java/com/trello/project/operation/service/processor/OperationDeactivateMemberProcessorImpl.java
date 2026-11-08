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

            // } catch (OperationManualReviewException e) {
            // throw e;
        } catch (Exception exception) {
            operationUtils.markManualReview(operation, exception);

            log.info("EXCEPTION: " + exception);
            throw new OperationManualReviewException();
        }
    }

    // TODO: DEFINIR UN BLOQUE CATCH CON LAS EXCEPCIONES DE TIPO RUNTIME EXCEPTION
    // DESDE EL MICROSERVICIO WORKFLOW
    @Override
    protected void processRemoteCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {
            workflowClientService.revertDeactivateBoardAccess(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException e) {
            operationUtils.markLocalCompensationCompleted(operation);
            throw new OperationCompensatedException();
        }
    }
}
