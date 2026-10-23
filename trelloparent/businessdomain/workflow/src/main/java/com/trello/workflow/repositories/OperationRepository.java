package com.trello.workflow.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar operación por ID de tablero y ID de usuario
    Optional<Operation> findByBoardIdAndUserIdAndType(UUID boardId, UUID userId, OperationType type);

    // TODO: DEFINIR UN METODO PARA LISTAR TODAS LAS OPERACIONES CON ESTADO
    // COMPLETED QUE SEAN DE TIPO ELIMINAR TABLERO O VARIOS TABLEROS Y QUE TENGAN EL
    // ESTADO EN FALSE PARA QUE PUEDA INICIAR EL PROCESO DE ELIMINACION
}