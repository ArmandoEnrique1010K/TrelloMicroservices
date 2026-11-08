package com.trello.project.operation.service.handler;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.exception.InvitationNotFoundException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotFoundException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.operation.service.utils.OperationHandlerUtils;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationAcceptInvitationServiceImpl implements OperationAcceptInvitationService {

    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;
    private final OperationHandlerUtils operationHandlerUtils;

    public OperationAcceptInvitationServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService,
            OperationHandlerUtils operationHandlerUtils) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
        this.operationHandlerUtils = operationHandlerUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyAcceptInvitation(UUID operationId, boolean automatic)
            throws OperationNotFoundException, OperationNotPendingException, InvitationConfirmedException {

        Operation operation = operationHandlerUtils
                .prepareExecution(operationId, automatic);

        // 1. Obtener los identificadores necesarios para localizar
        // la invitación y el miembro asociado a la aceptación.
        UUID boardId = operation.getBoardId();
        UUID recipientUserId = operation.getUserId();
        UUID invitationId = operation.getInvitationId();

        // 2. Recuperar la invitación y comprobar que todavía pueda aceptarse.
        // Si ya fue confirmada, no se deben aplicar nuevamente los cambios.
        Invitation invitation = invitationProjectService.findInvitationByIdAndRecipientUserId(
                invitationId,
                recipientUserId);

        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // 3. Confirmar la invitación y registrar la fecha de aceptación.
        invitation.setStatus(Status.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationProjectService.saveInvitation(invitation);

        // 4. Comprobar si el usuario ya tiene un registro de miembro
        // en el tablero para determinar si debe actualizarse o crearse.
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        Board board = invitation.getBoard();

        // 5. Aplicar los cambios correspondientes al miembro:
        // activar el registro existente y actualizar su rol, o crear
        // un nuevo miembro si todavía no existe.
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setActive(true);
            member.setRole(invitation.getRole());
            memberProjectService.saveMember(member);
        } else {
            // Si no existe el miembro, agregar el nuevo miembro
            Member member = new Member();
            member.setUserId(recipientUserId);
            member.setRole(invitation.getRole());
            member.setBoard(board);
            member.setActive(true);
            memberProjectService.saveMember(member);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAcceptInvitation(UUID operationId)
            throws OperationNotFoundException, InvitationNotFoundException, MemberNotFoundException {
        // 1. Recuperar la operación y registrar el inicio de la compensación
        // local para indicar que se están revirtiendo los cambios en Project.
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        operation.setStatus(OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS);
        operationProjectService.saveOperation(operation);

        // 2. Recuperar los datos originales registrados para la operación.
        // Se utilizan para restaurar el estado previo a la aceptación.
        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();
        UUID invitationId = operation.getInvitationId();
        Boolean activeMember = operation.getMemberActive();

        // Tomar el rol anterior
        Role role = operation.getOldRole();

        // 3. Revertir la aceptación de la invitación, restaurando
        // su estado y eliminando la fecha de aceptación.
        Invitation invitation = invitationProjectService.findInvitationByIdAndRecipientUserId(invitationId, userId);

        invitation.setStatus(Status.UNCONFIRMED);
        invitation.setAcceptedAt(null);
        invitationProjectService.saveInvitation(invitation);

        // 4. Buscar el registro de miembro para determinar cómo revertir
        // los cambios realizados durante la aceptación.
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, userId);

        // 5. Restaurar el estado previo del miembro según el valor
        // registrado antes de ejecutar la aceptación.
        if (activeMember == null) {

            // No existía un miembro antes de la operación.
            // Si se encuentra uno, eliminarlo porque fue creado durante
            // la aceptación de la invitación.
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();

                memberProjectService.deletePermanentlyMember(
                        member.getId(),
                        userId);
            } else {
                // No se encuentra el miembro que debía eliminarse.
                // Finalizar sin continuar con la actualización del estado
                // de compensación, según la lógica actual.

                // SE CONSIDERA QUE EL MIEMBRO YA FUE ELIMINADO
                return;

            }
        } else if (Boolean.FALSE.equals(activeMember)) {
            // El miembro existía antes de la operación, pero estaba inactivo.
            // Por tanto, debe conservarse y restaurarse su estado original.
            if (optionalMember.isEmpty()) {
                // El miembro que debía restaurarse no existe.
                // Registrar la inconsistencia para que requiera revisión manual.
                operation.setStatus(OperationStatus.MANUAL_INTERVENTION_REQUIRED);
                operationProjectService.saveOperation(operation);
                return;
            }

            // Restaurar el estado inactivo y el rol que tenía el miembro
            // antes de aceptar la invitación.
            Member member = optionalMember.get();
            member.setActive(false);
            member.setRole(role);
            memberProjectService.saveMember(member);
        } else {
            // El miembro estaba activo antes de la operación, lo que no
            // coincide con los escenarios admitidos para esta compensación.
            // Registrar la inconsistencia para que requiera revisión manual.
            operation.setStatus(OperationStatus.MANUAL_INTERVENTION_REQUIRED);
            operationProjectService.saveOperation(operation);
            return;
        }
    }
}
