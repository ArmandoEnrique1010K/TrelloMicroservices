package com.trello.workflow.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

    // Buscar operación por ID de tablero y ID de usuario
    Optional<Operation> findByBoardIdAndUserIdAndType(UUID boardId, UUID userId, OperationType type);

    // Listar todas las operaciones de un tablero
    List<Operation> findByBoardId(UUID boardId);

    // Actualizar el estado de todas las operaciones de un tablero
    @Modifying
    @Query("UPDATE Operation o SET o.status = :status WHERE o.boardId = :boardId")
    int updateStatusByBoardId(@Param("boardId") UUID boardId, @Param("status") OperationStatus status);

    // TODO: DEFINIR UN METODO PARA LISTAR TODAS LAS OPERACIONES CON ESTADO
    // COMPLETED QUE SEAN DE TIPO ELIMINAR TABLERO O VARIOS TABLEROS Y QUE TENGAN EL
    // ESTADO EN FALSE PARA QUE PUEDA INICIAR EL PROCESO DE ELIMINACION

}