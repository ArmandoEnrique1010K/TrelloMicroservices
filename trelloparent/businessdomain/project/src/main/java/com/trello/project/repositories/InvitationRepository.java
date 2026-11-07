package com.trello.project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.project.entities.Invitation;
import com.trello.project.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    // No se toma en cuenta el usuario emisor porque un tablero tiene un solo
    // usuario encargado de enviar invitaciones que es el mismo que ha creado el
    // board
    boolean existsByBoardIdAndRecipientUserId(UUID boardId, UUID recipientUserId);

    // Listar invitaciones por usuario receptor y ordenarlas de forma descendente
    // por fecha de envio
    List<Invitation> findByRecipientUserIdAndStatusOrderBySendedAtDesc(UUID recipientUserId, Status status);

    // Listar invitaciones por id de tablero y ordenarlas de forma descendente por
    // fecha de envio
    List<Invitation> findByBoardIdOrderBySendedAtDesc(UUID boardId);

    Optional<Invitation> findByIdAndSenderUserId(UUID id, UUID senderUserId);

    Optional<Invitation> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    // Optional<Invitation> findByBoardIdAndRecipientUserId(UUID boardId, UUID
    // recipientUserId);

    void deleteByBoardIdAndRecipientUserIdAndSenderUserId(
            UUID boardId,
            UUID recipientUserId,
            UUID senderUserId);

    @Modifying
    @Query("""
                DELETE FROM Invitation i
                WHERE i.acceptedAt <= :now
            """)
    void deleteAcceptedInvitations(@Param("now") LocalDateTime now);
}
