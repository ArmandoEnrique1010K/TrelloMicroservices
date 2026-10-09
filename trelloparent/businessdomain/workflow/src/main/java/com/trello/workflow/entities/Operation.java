package com.trello.workflow.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.Role;

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
// Representa los datos de una operación sincrona
public class Operation {

    // El ID no va a ser generado de forma automatica
    @Id
    private UUID id;

    // Tipo de operación
    // Relacionada con el nombre del método del endpoint
    @Enumerated(EnumType.STRING)
    private com.trello.workflow.enums.OperationType type;

    // Estado
    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    // Campos necesarios para cualquier operacion
    private UUID boardId;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private Role previousRole;
}
