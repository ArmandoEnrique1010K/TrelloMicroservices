package com.trello.project.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;

import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "operations")
// Representa los datos de una operación realizada
public class Operation {
    // El ID no va a ser generado de forma automatica
    @Id
    private UUID id;

    // Tipo de operación
    // Relacionada con el nombre del método del endpoint
    @Enumerated(EnumType.STRING)
    private OperationType type;

    // Estado de la operación
    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    // Campos necesarios para cualquier operacion
    private UUID boardId;

    private UUID userId;

    private UUID invitationId;

    // Miembro
    private UUID memberId;

    // Usuario autor del tablero
    private UUID ownerUserId;

    // Nombre del tablero
    private String boardName;
    // Descripción del tablero
    private String boardDescription;

    // ID de espacio de trabajo
    private UUID workspaceId;

    // Clave enviada desde el frontend
    private UUID idempotencyKey;

    // Boolean con B mayuscula para que admita los valores
    // null, true o false

    // El miembro esta previamente activo
    private Boolean memberActive;

    // Rol anterior del usuario
    @Enumerated(EnumType.STRING)
    private Role oldRole;

    // Rol nuevo del usuario
    @Enumerated(EnumType.STRING)
    private Role newRole;

    // Número de intentos realizados por el usuario
    private int manualAttempts;

    // Número de intentos realizados por la máquina
    // (ejecutor de tareas en segundo plano)
    private int automaticAttempts;

    // Fecha del siguiente intento automatico por máquina
    private LocalDateTime nextAutomaticAttemptAt;

    // Fecha de completado
    // Se llena cuando el campo OperationStatus tenga el estado de "COMPLETED" o
    // "COMPENSATED"
    private LocalDateTime completedAt;

    // Fecha de creación
    // @CreationTimestamp
    private LocalDateTime createdAt;

    // Fecha de actualización
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
