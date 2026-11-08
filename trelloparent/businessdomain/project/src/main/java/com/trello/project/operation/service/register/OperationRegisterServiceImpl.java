package com.trello.project.operation.service.register;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.board.dto.request.BoardRequest;
import com.trello.project.board.exception.BoardAlreadyExistsException;
import com.trello.project.entities.Board;
import com.trello.project.entities.Invitation;
import com.trello.project.entities.Member;
import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;
import com.trello.project.enums.Status;
import com.trello.project.exception.BoardNotFoundException;
import com.trello.project.exception.InvitationNotFoundException;
import com.trello.project.exception.MemberInactiveException;
import com.trello.project.exception.MemberNotFoundException;
import com.trello.project.exception.WorkspaceNotFoundException;
import com.trello.project.invitation.exception.InvitationConfirmedException;
import com.trello.project.operation.service.utils.OperationRegisterUtils;
import com.trello.project.service.BoardProjectService;
import com.trello.project.service.InvitationProjectService;
import com.trello.project.service.MemberProjectService;
import com.trello.project.service.OperationProjectService;
import com.trello.project.service.WorkspaceProjectService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationRegisterServiceImpl implements OperationRegisterService {

    private final OperationProjectService operationProjectService;
    private final InvitationProjectService invitationProjectService;
    private final MemberProjectService memberProjectService;
    private final BoardProjectService boardProjectService;
    private final WorkspaceProjectService workspaceProjectService;
    private final OperationRegisterUtils operationRegisterUtils;

    public OperationRegisterServiceImpl(
            OperationProjectService operationProjectService,
            InvitationProjectService invitationProjectService,
            MemberProjectService memberProjectService,
            BoardProjectService boardProjectService,
            WorkspaceProjectService workspaceProjectService,
            OperationRegisterUtils operationRegisterUtils) {
        this.operationProjectService = operationProjectService;
        this.invitationProjectService = invitationProjectService;
        this.memberProjectService = memberProjectService;
        this.boardProjectService = boardProjectService;
        this.workspaceProjectService = workspaceProjectService;
        this.operationRegisterUtils = operationRegisterUtils;
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
            throws InvitationNotFoundException, InvitationConfirmedException {

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
        Optional<Operation> existing = operationProjectService
                .findLatestOperationByInvitationIdAndType(
                        invitationId,
                        OperationType.ACCEPT_INVITATION);

        // Si existe una operación previa, evaluar su estado para determinar
        // si se puede reutilizar, si el proceso debe rechazarse o si corresponde
        // registrar una nueva operación.
        Operation reusable = operationRegisterUtils
                .findReusableOperation(existing);

        if (reusable != null) {
            // Actualizar el nuevo rol en la operación
            reusable.setNewRole(invitationRole);
            return reusable;
        }

        // 3. Obtener el tablero asociado a la invitación.
        Board findedBoard = invitation.getBoard();
        UUID boardId = findedBoard.getId();
        UUID workspaceId = findedBoard.getWorkspace().getId();

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
        Operation operation = operationRegisterUtils
                .createOperation(OperationType.ACCEPT_INVITATION);

        // Guardar las referencias del tablero, el usuario y la invitación
        // involucrados en la operación.
        operation.setBoardId(boardId);
        operation.setUserId(recipientUserId);
        operation.setInvitationId(invitationId);
        operation.setWorkspaceId(workspaceId);

        // Guardar el estado previo del miembro y los roles anterior y
        // previsto. Estos datos permiten conocer la situación inicial
        // y respaldar la lógica de compensación.
        operation.setMemberActive(memberActive);
        operation.setOldRole(role);
        operation.setNewRole(invitationRole);

        // 6. Persistir la operación en estado REGISTERED y devolverla
        // para que el flujo de aplicación de negocio pueda continuar.
        operationProjectService.saveOperation(operation);
        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerChangeRoleMember(Role role, UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException, MemberInactiveException {

        // Validar logica de negocio
        Member findedMember = memberProjectService.findMemberActiveByIdAndOwnerUserId(memberId, ownerUserId);

        // Buscar operación existente
        Optional<Operation> existing = operationProjectService.findLatestOperationByMemberIdAndType(memberId,
                OperationType.CHANGE_ROLE_MEMBER);

        Operation reusable = operationRegisterUtils
                .findReusableOperation(existing);

        if (reusable != null) {
            // Actualizar el nuevo rol en la operación
            reusable.setNewRole(role);
            return reusable;
        }

        Board findedBoard = findedMember.getBoard();
        UUID boardId = findedBoard.getId();
        UUID userId = findedMember.getUserId();
        UUID workspaceId = findedBoard.getWorkspace().getId();

        Operation operation = operationRegisterUtils
                .createOperation(OperationType.CHANGE_ROLE_MEMBER);

        // Solamente se rellenan los campos necesarios
        operation.setBoardId(boardId);

        // No confundir los usuarios:
        // ID del miembro
        operation.setMemberId(memberId);
        // ID del usuario miembro (toma el ID real del usuario)
        operation.setUserId(userId);
        // ID del usuario administrador del espacio de trabajo, que a su vez es el
        // administrador del tablero
        operation.setOwnerUserId(ownerUserId);

        // Se establece el antiguo rol
        operation.setOldRole(findedMember.getRole());
        // Y el nuevo rol aqui
        operation.setNewRole(role);

        operation.setWorkspaceId(workspaceId);

        operationProjectService.saveOperation(operation);
        return operation;
    }

    // TODO: SI DE FORMA AUTOMATICA SE DESACTIVA EL MIEMBRO, ENTONCES SE DEBEN
    // ELIMINAR LAS OPERACIONES PENDIENTES RELACIONADAS AL MIEMBRO POR ID Y
    // OWNERUSERID
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerDeactivateMember(UUID memberId, UUID ownerUserId)
            throws MemberNotFoundException, MemberInactiveException {

        Member findedMember = memberProjectService.findMemberActiveByIdAndOwnerUserId(memberId, ownerUserId);

        Optional<Operation> existing = operationProjectService.findLatestOperationByMemberIdAndType(memberId,
                OperationType.DEACTIVATE_MEMBER);

        Operation reusable = operationRegisterUtils
                .findReusableOperation(existing);

        if (reusable != null) {
            return reusable;
        }

        Board findedBoard = findedMember.getBoard();
        UUID boardId = findedBoard.getId();
        UUID userId = findedMember.getUserId();
        UUID workspaceId = findedBoard.getWorkspace().getId();

        Operation operation = operationRegisterUtils
                .createOperation(OperationType.DEACTIVATE_MEMBER);

        operation.setBoardId(boardId);
        operation.setMemberId(memberId);
        operation.setUserId(userId);
        operation.setOwnerUserId(ownerUserId);
        operation.setMemberActive(true);
        operation.setOldRole(findedMember.getRole());

        operation.setWorkspaceId(workspaceId);

        operationProjectService.saveOperation(operation);
        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerCreateBoard(UUID workspaceId, UUID ownerUserId,
            BoardRequest boardRequest,
            UUID idempotencyKey)
            throws WorkspaceNotFoundException {

        String name = boardRequest.getName();
        String description = boardRequest.getDescription();

        workspaceProjectService.findWorkspaceByIdAndOwnerUserIdAndStatusTrue(
                workspaceId,
                ownerUserId);

        if (boardProjectService.existsBoardByWorkspaceIdAndName(workspaceId, name)) {
            throw new BoardAlreadyExistsException();
        }

        // 1. Comprobar si ya se registró esta misma solicitud.
        Optional<Operation> existing = operationProjectService
                .findLatestOperationByWorkspaceIdAndIdempotencyKeyAndType(
                        workspaceId,
                        idempotencyKey,
                        OperationType.CREATE_BOARD);

        Operation reusable = operationRegisterUtils
                .findReusableOperation(existing);

        // 2. Si el usuario ha cambiado el nombre y la descripcion en el mismo
        // formulario, debe actualizarlo
        if (reusable != null) {
            reusable.setBoardName(name);
            reusable.setBoardDescription(description);
            return reusable;
        }

        // 3. Crear un identificador para el tablero.
        UUID boardId = UUID.randomUUID();

        Operation operation = operationRegisterUtils
                .createOperation(OperationType.CREATE_BOARD);

        operation.setBoardId(boardId);
        operation.setBoardName(name);
        operation.setBoardDescription(description);
        operation.setOwnerUserId(ownerUserId);
        operation.setWorkspaceId(workspaceId);

        // 4. Persistir la operación antes de ejecutar la creación.
        operationProjectService.saveOperation(operation);
        return operation;
    }

    // TODO: CONTINUAR
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerDeleteBoard(UUID ownerUserId, UUID boardId)
            throws WorkspaceNotFoundException, BoardNotFoundException {

        // Asegurar de que el tablero exista
        Board findedBoard = boardProjectService.findBoardByIdAndOwnerUserIdAndActive(boardId, ownerUserId);

        Optional<Operation> existing = operationProjectService.findLatestOperationByBoardIdAndType(boardId,
                OperationType.DELETE_BOARD);

        Operation reusable = operationRegisterUtils
                .findReusableOperation(existing);

        if (reusable != null) {
            return reusable;
        }

        UUID workspaceId = findedBoard.getWorkspace().getId();

        Operation operation = operationRegisterUtils
                .createOperation(OperationType.DELETE_BOARD);

        operation.setBoardId(boardId);
        operation.setOwnerUserId(ownerUserId);
        operation.setWorkspaceId(workspaceId);

        operationProjectService.saveOperation(operation);
        return operation;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Operation registerDeleteWorkspace(UUID ownerUserId, UUID workspaceId)
            throws WorkspaceNotFoundException {

        workspaceProjectService.findWorkspaceByIdAndOwnerUserIdAndStatusTrue(workspaceId, ownerUserId);

        Optional<Operation> existing = operationProjectService
                .findLatestOperationByWorkspaceIdAndType(
                        workspaceId,
                        OperationType.DELETE_WORKSPACE);

        Operation reusable = operationRegisterUtils
                .findReusableOperation(existing);

        if (reusable != null) {
            return reusable;
        }

        Operation operation = operationRegisterUtils
                .createOperation(OperationType.DELETE_WORKSPACE);

        operation.setWorkspaceId(workspaceId);
        operation.setOwnerUserId(ownerUserId);

        operationProjectService.saveOperation(operation);
        return operation;
    }
}
