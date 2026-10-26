package com.trello.project.operation.service.handler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

@Service
public class OperationChangeRoleMemberServiceImpl implements OperationChangeRoleMemberService {
    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;

    public OperationChangeRoleMemberServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyChangeRoleMember(UUID operationId, boolean automatic) {
        Operation persistedOperation = operationProjectService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        OperationStatus currentStatus = persistedOperation.getStatus();

        if (currentStatus != OperationStatus.REGISTERED
                && currentStatus != OperationStatus.LOCAL_COMPENSATION_COMPLETED
                && currentStatus != OperationStatus.COMPENSATION_COMPLETED) {
            throw new OperationNotPendingException();
        }

        if (automatic) {
            persistedOperation.setAutomaticAttempts(
                    persistedOperation.getAutomaticAttempts() + 1);
        } else {
            persistedOperation.setManualAttempts(
                    persistedOperation.getManualAttempts() + 1);
        }

        persistedOperation.setStatus(OperationStatus.EXECUTION_PENDING);
        persistedOperation.setCompletedAt(null);
        persistedOperation.setNextAutomaticAttemptAt(
                LocalDateTime.now().plusSeconds(30));

        UUID boardId = persistedOperation.getId();
        UUID userId = persistedOperation.getUserId();
        UUID memberId = persistedOperation.getMemberId();
        UUID ownerUserId = persistedOperation.getOwnerUserId();
        Role newRole = persistedOperation.getNewRole();

        // Buscar miembro
        Member findedMember = memberProjectService.findMemberActiveByIdAndOwnerUserId(memberId, ownerUserId);

        // Actualiza el rol
        findedMember.setRole(newRole);

        // Borrar invitación si existe
        invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
                boardId,
                userId,
                ownerUserId);

        memberProjectService.saveMember(findedMember);

        // Guardar la operación
        operationProjectService.saveOperation(persistedOperation);
    }

    // Solamente revierte los cambios en el rol del miembro
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertChangeRoleMember(UUID operationId) {
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);
        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        operationProjectService.saveOperation(operation);

        // UUID boardId = operation.getId();
        // UUID userId = operation.getUserId();
        UUID memberId = operation.getMemberId();
        UUID ownerUserId = operation.getOwnerUserId();
        Role oldRole = operation.getOldRole();

        Member findedMember = memberProjectService.findMemberActiveByIdAndOwnerUserId(memberId, ownerUserId);
        findedMember.setRole(oldRole);
        memberProjectService.saveMember(findedMember);
    }
}
