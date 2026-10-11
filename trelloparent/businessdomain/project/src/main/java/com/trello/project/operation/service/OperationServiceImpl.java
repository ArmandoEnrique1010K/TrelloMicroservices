package com.trello.project.operation.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.client.services.IdentityClientService;
import com.trello.project.client.services.WorkflowClientService;
import com.trello.project.client.utils.WorkflowRoleUtils;
import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Status;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.exception.ServiceUnavailableException;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.invitation.mapper.BoardInvitationResponseMapper;
import com.trello.project.invitation.mapper.InvitationRequestMapper;
import com.trello.project.invitation.mapper.InvitationResponseMapper;
import com.trello.project.invitation.mapper.ReceivedInvitationResponseMapper;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationServiceImpl implements OperationService {
    private final WorkflowClientService workflowClientService;
    private final BoardProjectService boardProjectService;
    private final InvitationRequestMapper invitationRequestMapper;
    private final InvitationProjectService invitationProjectService;
    private final InvitationResponseMapper invitationResponseMapper;
    private final MemberProjectService memberProjectService;
    private final IdentityClientService identityClientService;
    private final BoardInvitationResponseMapper boardInvitationResponseMapper;
    private final ReceivedInvitationResponseMapper receivedInvitationResponseMapper;
    private final OperationProjectService operationProjectService;

    public OperationServiceImpl(
            BoardProjectService boardProjectService,
            InvitationRequestMapper invitationRequestMapper, InvitationProjectService invitationProjectService,
            InvitationResponseMapper invitationResponseMapper,
            MemberProjectService memberProjectService, IdentityClientService identityClientService,
            BoardInvitationResponseMapper boardInvitationResponseMapper,
            ReceivedInvitationResponseMapper receivedInvitationResponseMapper,
            WorkflowClientService workflowClientService,
            OperationProjectService operationProjectService) {
        this.boardProjectService = boardProjectService;
        this.invitationRequestMapper = invitationRequestMapper;
        this.invitationProjectService = invitationProjectService;
        this.invitationResponseMapper = invitationResponseMapper;
        this.memberProjectService = memberProjectService;
        this.identityClientService = identityClientService;
        this.boardInvitationResponseMapper = boardInvitationResponseMapper;
        this.receivedInvitationResponseMapper = receivedInvitationResponseMapper;
        this.workflowClientService = workflowClientService;
        this.operationProjectService = operationProjectService;
    }

    // Registrar aceptación de invitación
    // Agrega una operación, pero aun no modifica la tabla Invitations
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UUID registerInvitationAcceptance(UUID invitationId, UUID recipientUserId)
            throws InvitationConfirmedException {

        // // ============================================================
        // // Idempotencia
        // // ============================================================
        // // Buscar una operación por tipo e ID de invitación
        Optional<Operation> existingOperation = operationProjectService.findOperationByInvitationIdAndType(
                invitationId,
                OperationType.ACCEPT_INVITATION);

        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {

                case PENDING, COMPENSATION_PENDING -> {
                    return existing.getId();
                }

                case CREATED, PROCESSING, COMPENSATION_PROCESSING, COMPLETED, MANUAL_REVIEW, COMPENSATED ->
                    // TODO: AÑADIR NUEVA EXCEPCIÓN
                    throw new BusinessRuleException();
            }

            // TODO: EJECUTAR INMEDIATAMENTE EL MÉTODO DEFINIDO EN SCHEDULER SI ESTA EN
            // PENDING
        }

        // 1. Buscar y validar la invitación.
        // Busca la invitación por ID y usuario receptor
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        invitationId,
                        recipientUserId);

        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // Busca el tablero correspondiente a la invitación
        Board findedBoard = invitation.getBoard();

        // 2. Buscar si existe un miembro previo.
        // Solamente si al buscar miembro por userId y boardId, este ya se encuentra en
        // la base de datos porque el miembro ha sido desactivado previamente, entonces
        // se va a volver a activar (campo active pasa a true) y se cambia el rol (si ha
        // cambiado cuando se le ha invitado al usuario)
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(findedBoard.getId(), recipientUserId);

        // 3. Registrar la operación antes de modificar los datos.
        // Registrar la operación en la misma transacción.

        // Genera un ID unico para la operación
        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACCEPT_INVITATION);
        operation.setStatus(OperationStatus.CREATED);

        // Campos necesarios
        operation.setBoardId(findedBoard.getId());
        operation.setUserId(recipientUserId);
        operation.setInvitationId(invitationId);
        operation.setAttempts(0);

        // Estado previo real: null si no existía el miembro.
        operation.setMemberActive(
                optionalMember
                        .map(member -> member.isActive())
                        .orElse(null));
        operation.setRole(optionalMember.map(member -> member.getRole()).orElse(null));

        operationProjectService.saveOperation(operation);

        return operationId;
    }

    // Obtiene la operacion por ID y trata de hacer la modificacion en la tabla
    // Invitations
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyInvitationAcceptance(UUID operationId) throws InvitationConfirmedException {
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(BusinessRuleException::new);

        operation.setStatus(OperationStatus.PENDING);

        UUID boardId = operation.getBoardId();
        UUID recipientUserId = operation.getUserId();
        UUID invitationId = operation.getInvitationId();

        Invitation invitation = invitationProjectService.findInvitationByIdAndRecipientUserId(
                invitationId,
                recipientUserId);

        // Validar que la invitación todavía pueda aceptarse.
        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // Actualizar la invitación a ACCEPTED.
        invitation.setStatus(Status.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationProjectService.saveInvitation(invitation);

        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        Board findedBoard = invitation.getBoard();

        // Crear o reactivar el miembro.
        // Crear o reactivar el Member, según corresponda.
        if (optionalMember.isPresent()) {
            // Activar miembro inactivo
            Member member = optionalMember.get();
            member.setActive(true);
            member.setRole(invitation.getRole());

            // Los campos userId y Board ya se encuentran guardados previamente
            memberProjectService.saveMember(member);
        } else {
            // Crear nuevo miembro
            // Agregar usuario receptor como miembro del board
            Member member = new Member();
            member.setUserId(recipientUserId);
            member.setRole(invitation.getRole());
            member.setBoard(findedBoard);
            member.setActive(true);
            // Guardar los cambios.
            memberProjectService.saveMember(member);
        }

        operationProjectService.saveOperation(operation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertInvitationAcceptance(UUID operationId) {
        // Recordar que en operation se almacena los datos de la operacion antes de
        // hacer la modificacion en la base de datos
        Operation operation = operationProjectService.findOperationById(operationId)
                .orElseThrow(BusinessRuleException::new);

        // Revertir la operación
        operation.setStatus(OperationStatus.COMPENSATION_PROCESSING);
        operationProjectService.saveOperation(operation);

        UUID boardId = operation.getBoardId();
        UUID userId = operation.getUserId();

        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, userId);

        // En la tabla Invitations debe revertir los cambios
        Boolean activeMember = operation.getMemberActive();

        log.info("Operation ID: {}", operationId);
        log.info("Board ID: {}", boardId);
        log.info("User ID: {}", userId);
        log.info("Member active antes de la operación: {}", activeMember);
        log.info("¿Existe actualmente el miembro?: {}", optionalMember.isPresent());

        // Obtener la invitación para modificarlo
        UUID invitationId = operation.getInvitationId();
        Invitation invitation = invitationProjectService.findInvitationByIdAndRecipientUserId(invitationId, userId);

        invitation.setStatus(Status.UNCONFIRMED);
        invitation.setAcceptedAt(null);
        invitationProjectService.saveInvitation(invitation);

        // Member active es null si nunca ha sido miembro
        // false si el miembro ha estado inactivo
        if (activeMember == null) {
            log.info("EL MIEMBRO ACTIVO ES NULO, ERROR <---");

            // AQUI CAE

            // Un miembro puede existir o no existir
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();

                log.info("Eliminando miembro con ID: {}", member.getId());

                // Eliminar miembro
                memberProjectService.deletePermanentlyMember(
                        member.getId(),
                        userId);
                log.info("Se ejecutó deletePermanentlyMember");

            } else {
                log.warn(
                        "No se encontró el miembro que debía eliminarse. Board: {}, User: {}",
                        boardId,
                        userId);
            }
        } else if (Boolean.FALSE.equals(activeMember)) {
            log.info("CAMBIANDO ESTADO");

            if (optionalMember.isEmpty()) {
                operation.setStatus(OperationStatus.MANUAL_REVIEW);
                operationProjectService.saveOperation(operation);
                return;
            }

            // Modificar miembro
            Member member = optionalMember.get();
            member.setActive(false);
            // Restaurar el rol anterior
            member.setRole(operation.getRole());
            memberProjectService.saveMember(member);
        } else {
            log.info("ERROR DEL SISTEMA");
            // Aunque esto nunca podra pasar porque es imposible que un miembro haya
            // registrado true en el campo operation.getMemberActive()
            operation.setStatus(OperationStatus.MANUAL_REVIEW);
            operationProjectService.saveOperation(operation);
            return;
        }

        // La operación se ha compensado
        operation.setStatus(OperationStatus.COMPENSATED);
        operation.setCompletedAt(LocalDateTime.now());
        operationProjectService.saveOperation(operation);
    }
}
