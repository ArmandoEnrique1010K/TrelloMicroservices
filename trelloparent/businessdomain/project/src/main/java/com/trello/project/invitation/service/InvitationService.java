package com.trello.project.invitation.service;

import java.util.List;
import java.util.UUID;

import com.trello.project.client.dto.response.UserResponse;
import com.trello.project.invitation.dto.request.InvitationRequest;
import com.trello.project.invitation.dto.response.BoardInvitationResponse;
import com.trello.project.invitation.dto.response.InvitationResponse;
import com.trello.project.invitation.dto.response.ReceivedInvitationResponse;
import com.trello.project.invitation.exception.InvitationAlreadyExistsException;

public interface InvitationService {

    InvitationResponse sendInvitation(UUID boardId, UUID ownerUserId, UUID recipientUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException;

    // Listar invitaciones por ID de usuario receptor (invitaciones recibidas por
    // usuario autenticado)
    List<ReceivedInvitationResponse> listAllInvitationsByRecipientUserId(UUID recipientUserId);

    // Listar invitaciones por ID de tablero (solamente para administrador de
    // tablero)
    List<BoardInvitationResponse> listAllInvitationsByBoardId(UUID boardId, UUID ownerUserId);

    // Editar una invitación existente
    InvitationResponse editInvitation(UUID invitationId, InvitationRequest invitationRequest, UUID ownerUserId);

    // Eliminar invitación
    // Una invitación que aun no ha sido confirmada, si se elimina, se elimina la
    // invitación del sistema
    // Acceso por parte del administrador del tablero
    void deleteInvitation(UUID invitationId, UUID ownerUserId);

    // Aceptar invitación (por el usuario receptor)
    // Y agregar al usuario receptor como miembro del tablero

    // Una invitación aceptada ya puede ser eliminada sin problema porque ya es un
    // miembro del tablero
    void acceptInvitation(UUID invitationId, UUID recipientUserId);

    // Rechazar invitación
    // Acceso por parte del usuario receptor
    void declineInvitation(UUID invitationId, UUID recipientUserId);

    // Reenviar invitación
    // Cuando un miembro ha sido desactivado y se quiere volver a activar
    InvitationResponse resendInvitation(UUID boardId, UUID ownerUserId, UUID memberUserId,
            InvitationRequest invitationRequest) throws InvitationAlreadyExistsException;

    // Listar los usuarios disponibles para ser invitados al tablero
    List<UserResponse> listAllAvailableUsers(UUID boardId, UUID ownerUserId, String email);
}
