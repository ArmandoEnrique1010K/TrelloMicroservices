package com.trello.project.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
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

    private boolean memberActive;

    // Rol del usuario
    @Enumerated(EnumType.STRING)
    private Role role;

    // Fecha de creación
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Fecha de actualización
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Intentos
    private int attempts;

    // Fecha del siguiente intento
    private LocalDateTime nextAttemptAt;

    // Fecha de completado
    private LocalDateTime completedAt;

    // Log auxiliar del ultimo error
    private String lastError;

}
