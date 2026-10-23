package com.trello.project.operation.service.register;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.exception.OperationNotPendingException;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationRegisterServiceImpl implements OperationRegisterService {

    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;

    public OperationRegisterServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
    }

    // Recordar que si cae en una excepción, revertira los cambios realizados en la
    // base de datos
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationConfirmedException, OperationNotPendingException {

        // 1. Idempotencia
        // Buscar una operación por tipo e ID de invitación
        // Este metodo devuelve
        Optional<Operation> existingOperation = operationProjectService.findOperationByInvitationIdAndType(
                invitationId,
                OperationType.ACCEPT_INVITATION);

        // Nota: Primero debe tomar en cuenta la logica de negocio antes de lanzar una
        // excepcion relacionada a operaciones

        // 2. Buscar y validar la invitación
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        invitationId,
                        recipientUserId);

        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // 3. No registrar nuevamente la operación si existe
        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {
                case REGISTERED, LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED -> {
                    // Terminar aqui la ejecución del metodo y devolver la entidad
                    return existing;
                }

                // Lanzar una excepción si la operación no esta en un estado pendiente
                case EXECUTION_PENDING,
                        EXECUTING,
                        COMPLETED, COMPENSATION_PENDING,
                        LOCAL_COMPENSATION_IN_PROGRESS,
                        MANUAL_INTERVENTION_REQUIRED ->
                    throw new OperationNotPendingException();
            }
        }

        // Busca el tablero correspondiente a la invitación
        Board findedBoard = invitation.getBoard();
        UUID boardId = findedBoard.getId();

        // 4. Buscar si existe un miembro
        // Si el miembro existe quiere decir que se trata de un miembro que ha sido
        // eliminado del equipo de un tablero
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        // Puede ser un null, false o true
        Boolean memberActive = optionalMember
                .map(m -> m.isActive())
                .orElse(null);

        // Puede ser un null o un Role (que es el rol antiguo)
        Role role = optionalMember
                .map(m -> m.getRole())
                .orElse(null);

        // 5. Registrar la operación antes de alterar los datos relacionados a
        // Invitation y Member

        // Genera un ID unico para la operación
        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACCEPT_INVITATION);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Solamente se rellenan los campos necesarios
        operation.setBoardId(boardId);
        operation.setUserId(recipientUserId);
        operation.setInvitationId(invitationId);

        operation.setMemberActive(memberActive);
        operation.setNewRole(role);

        // Fecha actual en la fecha de creación
        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        // 6. Guardar operación y devolver la entidad
        operationProjectService.saveOperation(operation);
        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerChangeRoleMember(Role role, UUID memberId, UUID ownerUserId) {

        // En este caso la lógica para buscar la operación es distinta
        // El problema es que para cambiar un rol se realiza una operación sin importar
        // si se trata del mismo miembro por ID y/o administrador del espacio de trabajo

        // TODO: VERIFICAR SI CUMPLE LA CONDICION CUANDO EL MICROSERVICIO SE CAE Y
        // CUANDO EL MICROSERVICIO WORKFLOW TIENE UN RETRASO
        // Lo unico que no cambia es el estado de la operación
        // Si tiene el estado COMPLETED, debe crear una nueva operación
        List<OperationStatus> blockingStatuses = List.of(
                OperationStatus.REGISTERED,
                OperationStatus.EXECUTION_PENDING,
                OperationStatus.EXECUTING,
                OperationStatus.COMPENSATION_PENDING,
                OperationStatus.LOCAL_COMPENSATION_IN_PROGRESS,
                OperationStatus.MANUAL_INTERVENTION_REQUIRED);

        // Validar logica de negocio
        Member findedMember = memberProjectService.findMemberByIdAndOwnerUserId(memberId, ownerUserId);

        List<Operation> existingOperations = operationProjectService
                .findByMemberIdAndTypeAndStatusInOrdenByCreatedAtDesc(
                        memberId,
                        OperationType.CHANGE_ROLE_MEMBER,
                        blockingStatuses);

        // Optional<Operation> existingOperation =
        // operationProjectService.findOperationByMemberIdAndType(
        // memberId,
        // OperationType.CHANGE_ROLE_MEMBER);

        if (!existingOperations.isEmpty()) {
            Operation existing = existingOperations.get(0);

            switch (existing.getStatus()) {
                case REGISTERED, LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED -> {
                    // Terminar aqui la ejecución del metodo y devolver la entidad
                    return existing;
                }

                // Lanzar una excepción si la operación no esta en un estado pendiente
                case EXECUTION_PENDING,
                        EXECUTING,
                        COMPENSATION_PENDING,
                        LOCAL_COMPENSATION_IN_PROGRESS,
                        MANUAL_INTERVENTION_REQUIRED ->
                    throw new OperationNotPendingException();

                case COMPLETED -> {
                    // Se ignora el estado completado para continuar con la lógica posterior
                }
            }
        }

        // Busca el tablero
        Board findedBoard = findedMember.getBoard();
        UUID boardId = findedBoard.getId();
        // Busca usuario
        UUID userId = findedMember.getUserId();

        // Genera un ID unico para la operación
        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.CHANGE_ROLE_MEMBER);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Solamente se rellenan los campos necesarios
        operation.setBoardId(boardId);
        operation.setUserId(userId);
        operation.setMemberId(memberId);
        operation.setOwnerUserId(ownerUserId);

        // Se establece el antiguo rol
        operation.setOldRole(findedMember.getRole());
        // El nuevo rol aqui
        operation.setNewRole(role);

        // Fecha actual en la fecha de creación
        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        // 6. Guardar operación y devolver la entidad
        operationProjectService.saveOperation(operation);
        return operation;
    }
}
