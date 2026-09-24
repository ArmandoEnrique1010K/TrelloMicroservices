package com.trello.workflow.entities;

import java.util.UUID;

import com.trello.workflow.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// Esta entidad representa una copia de los datos obtenidos del microservicio Project
// Establece los limites para gestionar una tarea (TASK)
@Entity
@Data
@Table(name = "board_access")
public class BoardAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "board_Id", nullable = false)
    private UUID boardId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private boolean userActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
