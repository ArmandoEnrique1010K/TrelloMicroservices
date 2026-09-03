package com.trello.project.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.trello.project.enums.Role;
import com.trello.project.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "invitations")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // ID del usuario emisor
    @Column(name = "inviter_user_id", nullable = false)
    private UUID inviterUserId;

    // ID del usuario receptor
    // Solamente se invita a usuarios registrados
    @Column(name = "invited_user_id", nullable = false)
    private UUID invitedUserId;

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Estado de la aceptación: aceptada o rechazada
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
