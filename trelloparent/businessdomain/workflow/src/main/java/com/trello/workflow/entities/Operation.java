package com.trello.workflow.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Operacion realizada en el microservicio Workflow
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "operations")
public class Operation {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    // Campos necesarios
    private UUID boardId;

    private UUID userId;

    // Antiguo dato del usuario activo
    private Boolean oldUserActive;

    // Antiguo dato del rol
    @Enumerated(EnumType.STRING)
    private Role oldRole;

    // Nuevo rol
    @Enumerated(EnumType.STRING)
    private Role newRole;

    // Número de intentos
    private int attempts;

    // A diferencia de la entidad Operation del microservicio Project, las
    // operaciones no se van a ejecutar con un @Scheduler sino con una petición
    // realizada
    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
