package com.trello.workflow.entities;

import java.time.LocalDateTime;
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

    // Acceso del tablero
    // true = si tiene acceso
    // false = si no tiene acceso y va a ser eliminado
    private boolean status;

    // Fecha y hora en que se realizó el borrado lógico del tablero.
    // Se utiliza como referencia para determinar cuándo debe ejecutarse su
    // eliminación física mediante un proceso en segundo plano.
    // El proceso eliminará físicamente los tableros cuyo estado sea false y que
    // hayan superado un período de retención de 5 minutos desde su fecha de
    // eliminación.
    // Este período permite revertir la operación de borrado lógico antes de que el
    // tablero sea eliminado físicamente, restaurando su estado a true.
    private LocalDateTime deletedAt;

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
