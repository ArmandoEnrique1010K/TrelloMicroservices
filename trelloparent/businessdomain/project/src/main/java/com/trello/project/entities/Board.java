package com.trello.project.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "boards",
        // La combinación de workspace_id y name debe ser única para evitar crear
        // tableros con el mismo nombre dentro del mismo workspace.
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_workspace_name", columnNames = { "workspace_id", "name" })
        })
public class Board {
    @Id
    // No sera generado de forma automatica
    // @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    private String name;
    private String description;

    // No se utiliza la siguiente anotacion porque la fecha de creación sera
    // mostrada en la respuesta cuando se crea un tablero
    // @CreationTimestamp
    // @Column(nullable = false, updatable = false)
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Estado del tablero, si es false quiere decir que fue eliminado logicamente
    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invitation> invitations;

}
