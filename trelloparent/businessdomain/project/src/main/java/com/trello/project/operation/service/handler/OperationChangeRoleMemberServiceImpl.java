package com.trello.project.operation.service.handler;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Board;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.BoardStatus;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.utils.OperationHandlerUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

@Service
public class OperationChangeRoleMemberServiceImpl implements OperationChangeRoleMemberService {
    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;
    private final OperationHandlerUtils operationHandlerUtils;
    private final BoardProjectService boardProjectService;

    public OperationChangeRoleMemberServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService,
            OperationHandlerUtils operationHandlerUtils,
            BoardProjectService boardProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
        this.operationHandlerUtils = operationHandlerUtils;
        this.boardProjectService = boardProjectService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyChangeRoleMember(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, MemberNotFoundException,
            MemberInactiveException {

        // Este metodo cambia el estado a EXECUTION_PENDING
        Operation operation = operationHandlerUtils
                .prepareExecution(operationId, automatic);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        UUID memberId = operation.getMemberId();
        UUID ownerUserId = operation.getOwnerUserId();
        Role newRole = operation.getNewRole();

        // Bloqueo pesimista: coordina la ejecución de esta operación
        // con otras operaciones que intenten modificar el mismo tablero.
        Board board = boardProjectService.findBoardByIdWithLock(boardId);

        // Verificar que el tablero siga habilitado.
        if (board.getStatus() != BoardStatus.ACTIVE) {
            throw new BoardNotFoundException();
        }

        // Buscar miembro
        Member findedMember = memberProjectService.findMemberActiveByIdAndOwnerUserId(memberId, ownerUserId);

        // Actualiza el rol
        findedMember.setRole(newRole);

        // Caso especial
        // Borrar invitación si existe

        // Luego de cambiar el rol de un miembro, si o si debe eliminar la invitación,
        // porque en la invitación se ha especificado un rol y cuando cambia el rol, el
        // rol escrito en la invitación ya no tiene validez, a pesar de que se haya
        // aceptado la invitación porque solamente se puede cambiar el rol del miembro
        // cuando el miembro acepto la invitación o tambien antes de que acepte la
        // invitación, se puede cambiar el rol de la invitación
        invitationProjectService.deleteInvitationIfExistsByBoardIdAndRecipientUserId(
                boardId,
                userId,
                ownerUserId);

        memberProjectService.saveMember(findedMember);
    }

    // Solamente revierte los cambios en el rol del miembro
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertChangeRoleMember(UUID operationId)
            throws OperationNotFoundException, MemberNotFoundException, MemberInactiveException {
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
