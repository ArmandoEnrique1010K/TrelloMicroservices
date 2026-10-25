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

    /**
     * Registra una operación para aceptar una invitación.
     *
     * Valida las condiciones de negocio, comprueba si existe una operación
     * previa que pueda reutilizarse y registra el estado inicial junto con
     * la información necesaria para ejecutar o compensar la operación.
     *
     * La operación se registra antes de que otro servicio aplique los cambios
     * de negocio sobre Invitation y Member.
     *
     * Si se produce una excepción que provoque el rollback, los cambios
     * realizados dentro de esta transacción se revierten.
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerAcceptInvitation(UUID invitationId, UUID recipientUserId)
            throws InvitationConfirmedException, OperationNotPendingException {

        // 1. Validar las condiciones de negocio antes de comprobar
        // si existe una operación previa que pueda reutilizarse.
        Invitation invitation = invitationProjectService
                .findInvitationByIdAndRecipientUserId(
                        invitationId,
                        recipientUserId);

        // La invitación solo puede aceptarse si todavía no ha sido confirmada.
        if (invitation.getStatus() != Status.UNCONFIRMED) {
            throw new InvitationConfirmedException();
        }

        // Capturar el rol actual de la invitación para registrar el rol
        // que se asignará al usuario al aceptar la invitación.
        Role invitationRole = invitation.getRole();

        // 2. Buscar una operación previa de aceptación de la invitación que
        // pueda reutilizarse para continuar un intento anterior.
        Optional<Operation> existingOperation = operationProjectService
                .findLatestOperationByInvitationIdAndType(
                        invitationId,
                        OperationType.ACCEPT_INVITATION);

        // Si existe una operación previa, evaluar su estado para determinar
        // si se puede reutilizar, si el proceso debe rechazarse o si corresponde
        // registrar una nueva operación.
        if (existingOperation.isPresent()) {
            Operation existing = existingOperation.get();

            switch (existing.getStatus()) {
                case REGISTERED, LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED -> {
                    // Reutilizar la operación existente para reintentar el proceso
                    // conservando su identificador.
                    //
                    // Actualizar el nuevo rol con el valor actual de la invitación,
                    // ya que este puede haber cambiado desde el intento anterior.
                    existing.setNewRole(invitation.getRole());

                    // Devolver la operación existente para continuar el flujo
                    // utilizando el mismo identificador.
                    return existing;
                }

                // La operación se encuentra en ejecución, pendiente de ejecución
                // o compensación, o requiere intervención manual. No se permite
                // iniciar otro intento mientras esta operación no se resuelva.
                case EXECUTION_PENDING,
                        EXECUTING,
                        COMPENSATION_PENDING,
                        LOCAL_COMPENSATION_IN_PROGRESS,
                        MANUAL_INTERVENTION_REQUIRED ->
                    throw new OperationNotPendingException();

                case COMPLETED -> {
                    // La operación anterior finalizó correctamente. No se reutiliza;
                    // se continúa con el flujo para registrar una nueva operación
                    // de aceptación de la invitación.
                }
            }
        }

        // 3. Obtener el tablero asociado a la invitación.
        Board findedBoard = invitation.getBoard();
        UUID boardId = findedBoard.getId();

        // 4. Consultar si ya existe un registro de miembro para el usuario
        // en este tablero.
        //
        // La existencia de un miembro puede indicar que el usuario fue
        // retirado previamente del tablero. En ese caso, su registro puede
        // necesitar reactivación en lugar de una creación desde cero.
        Optional<Member> optionalMember = memberProjectService
                .findOptionalMemberByBoardIdAndUserId(boardId, recipientUserId);

        // Capturar el estado de actividad anterior del miembro.
        // null indica que no existe un registro previo.
        Boolean memberActive = optionalMember
                .map(m -> m.isActive())
                .orElse(null);

        // Capturar el rol anterior del miembro para disponer de la
        // información necesaria durante una eventual compensación.
        // null indica que no existe un registro previo.
        Role role = optionalMember
                .map(m -> m.getRole())
                .orElse(null);

        // 5. Construir la operación antes de aplicar modificaciones sobre
        // Invitation y Member.
        //
        // Se registran los valores previos y el nuevo rol previsto para
        // disponer de información suficiente para gestionar la ejecución
        // y, si fuera necesario, la compensación de los cambios locales.

        // Generar el identificador que permitirá relacionar los pasos
        // posteriores de la operación.
        UUID operationId = UUID.randomUUID();

        Operation operation = new Operation();
        operation.setId(operationId);
        operation.setType(OperationType.ACCEPT_INVITATION);
        operation.setStatus(OperationStatus.REGISTERED);
        operation.setManualAttempts(0);
        operation.setAutomaticAttempts(0);

        // Guardar las referencias del tablero, el usuario y la invitación
        // involucrados en la operación.
        operation.setBoardId(boardId);
        operation.setUserId(recipientUserId);
        operation.setInvitationId(invitationId);

        // Guardar el estado previo del miembro y los roles anterior y
        // previsto. Estos datos permiten conocer la situación inicial
        // y respaldar la lógica de compensación.
        operation.setMemberActive(memberActive);
        operation.setOldRole(role);
        operation.setNewRole(invitationRole);

        // Registrar la fecha de creación y dejar sin programar el siguiente
        // intento automático hasta que corresponda según el flujo.
        LocalDateTime currentDateTime = LocalDateTime.now();
        operation.setCreatedAt(currentDateTime);
        operation.setNextAutomaticAttemptAt(null);

        // 6. Persistir la operación en estado REGISTERED y devolverla
        // para que el flujo de aplicación de negocio pueda continuar.
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

        // Da una lista de las operaciones por Id de miembro, tipo y que tenga uno de
        // los estados bloqueantes, ordena de forma descendente por fecha de creación
        List<Operation> existingOperations = operationProjectService
                .findByMemberIdAndTypeAndStatusInOrderByCreatedAtDesc(
                        memberId,
                        OperationType.CHANGE_ROLE_MEMBER,
                        blockingStatuses);

        // Optional<Operation> existingOperation =
        // operationProjectService.findOperationByMemberIdAndType(
        // memberId,
        // OperationType.CHANGE_ROLE_MEMBER);

        if (!existingOperations.isEmpty()) {
            // Solamente toma el primero de las operaciones si existe al menos una operación
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
