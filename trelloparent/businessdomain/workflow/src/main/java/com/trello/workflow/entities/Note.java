package com.trello.workflow.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private UUID createdByUserId;

    // En este caso no se va a utilizar la anotación para escribir la fecha de
    // creación de forma automatica porque solamente lo guarda en la base de datos
    // en el momento en el que se crea el registro, pero no se puede obtener la
    // fecha directamente en la respuesta del usuario

    // Por ello se opta por el uso de LocalDateTime.now en el servicio de createNote
    // @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
