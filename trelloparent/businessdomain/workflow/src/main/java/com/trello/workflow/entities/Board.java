package com.trello.workflow.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "boards")
public class Board {

    // El ID es generado por el microservicio Project y reutilizado
    // como identificador del Board en Workflow.
    @Id
    private UUID id;

    // CascadeType.REMOVE permite propagar la eliminación del Board a las entidades
    // relacionadas, eliminando automáticamente sus BoardAccess y Task asociados.
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardAccess> boardAccesses;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Task> tasks;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    // En este caso se importa aqui la entidad Label, porque la palabra Label en
    // Java es una palabra reservada
    private List<com.trello.workflow.entities.Label> labels;
}
