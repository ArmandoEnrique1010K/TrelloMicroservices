package com.trello.project.operation.service.processor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.member.dto.response.MemberResponse;
import com.trello.project.member.mapper.MemberResponseMapper;
import com.trello.project.operation.exception.OperationCompensatedException;
import com.trello.project.operation.exception.OperationConflictException;
import com.trello.project.operation.exception.OperationManualReviewException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.handler.OperationChangeRoleMemberService;
import com.trello.project.operation.service.utils.AbstractOperationProcessor;
import com.trello.project.operation.service.utils.OperationUtils;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationChangeRoleMemberProcessorImpl
        extends AbstractOperationProcessor
        implements OperationChangeRoleMemberProcessor {

    private final WorkflowClientService workflowClientService;
    private final MemberProjectService memberProjectService;
    private final OperationChangeRoleMemberService operationChangeRoleMemberService;
    private final MemberResponseMapper memberResponseMapper;

    public OperationChangeRoleMemberProcessorImpl(
            OperationUtils operationUtils,
            OperationProjectService operationProjectService,
            OperationChangeRoleMemberService operationChangeRoleMemberService,
            WorkflowClientService workflowClientService,
            MemberProjectService memberProjectService,
            MemberResponseMapper memberResponseMapper) {

        super(operationUtils, operationProjectService);

        this.workflowClientService = workflowClientService;
        this.operationChangeRoleMemberService = operationChangeRoleMemberService;
        this.memberProjectService = memberProjectService;
        this.memberResponseMapper = memberResponseMapper;
    }

    // En lugar de un void se devuelve un MemberResponse
    @Override
    public MemberResponse processChangeRoleMemberOperation(UUID operationId)
            throws OperationNotFoundException, OperationConflictException, MemberNotFoundException,
            OperationNotPendingException, OperationManualReviewException, MemberInactiveException,
            OperationCompensatedException, BusinessRuleException

    {
        Operation operation = findOperation(operationId);

        if (operation.getType() != OperationType.CHANGE_ROLE_MEMBER) {
            throw new OperationConflictException();
        }

        processOriginalOperation(operation);

        // Luego de procesar la operación debe realizar el mapeo
        UUID memberId = operation.getMemberId();
        UUID userId = operation.getUserId();

        Member member = memberProjectService.findMemberByIdAndUserId(memberId,
                userId);
        MemberResponse memberResponse = memberResponseMapper.memberToMemberResponse(member);
        return memberResponse;
    }

    @Override
    protected void executeOriginalOperation(Operation operation) {
        UUID memberId = operation.getMemberId();
        UUID userId = operation.getUserId();
        UUID boardId = operation.getBoardId();
        Role oldRole = operation.getOldRole();
        Role newRole = operation.getNewRole();

        WorkflowRole workflowNewRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                newRole);

        // Cambiar el rol antiguo
        WorkflowRole workflowOldRole = WorkflowRoleUtils.parseRoleToWorkflowRole(
                oldRole);

        // Verificar que el miembro exista
        memberProjectService.findMemberByIdAndUserId(memberId, userId);

        workflowClientService.changeRoleBoardAccess(boardId, userId, workflowNewRole, workflowOldRole,
                operation.getId());
    }

    @Override
    protected void processLocalCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.COMPENSATION_PENDING) {
            throw new OperationNotPendingException();
        }

        try {
            operationUtils.markLocalCompensationInProgress(operation);
            operationChangeRoleMemberService.revertChangeRoleMember(operation.getId());

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

    // // TODO: DEFINIR UN BLOQUE CATCH CON LAS EXCEPCIONES DE TIPO RUNTIME
    // EXCEPTION
    // // DESDE EL MICROSERVICIO WORKFLOW
    @Override
    protected void processRemoteCompensation(Operation operation) {
        if (operation.getStatus() != OperationStatus.LOCAL_COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        try {
            workflowClientService.revertChangeRoleBoardAccess(operation.getId());
            operationUtils.markCompensationCompleted(operation);
        } catch (ServiceUnavailableException exception) {
            operationUtils.markLocalCompensationCompleted(operation);
            log.info("EXCEPTION: " + exception);
            throw new OperationCompensatedException();
        }
    }
}
