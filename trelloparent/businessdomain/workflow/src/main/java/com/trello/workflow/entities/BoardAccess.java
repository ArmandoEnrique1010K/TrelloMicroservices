package com.trello.workflow.entities;

import java.util.UUID;

import com.trello.workflow.enums.Role;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Esta entidad representa una copia de los datos obtenidos del microservicio Project
// Establece los limites para gestionar una tarea (TASK)
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "board_accesses", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "board_id", "user_id" })
})
public class BoardAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private boolean userActive;

    // Estado del permiso de acceso
    private boolean status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
