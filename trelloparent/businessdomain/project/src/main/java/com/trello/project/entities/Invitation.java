package com.trello.project.entities;

import java.util.UUID;

import com.trello.project.enums.Role;

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

    // ID del usuario receptor
    private UUID receiverId;

    private String message;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

}
