package com.trello.project.operation.service.executor;

public interface OperationExecutorService {
    // Metodo para ejecutar operaciones en estado pendiente
    // Estados permitidos: LOCAL_COMPENSATION_COMPLETED, COMPENSATION_COMPLETED
    void executeAllCompensatedOperations();

    // Borrar todas los tableros y espacios de trabajo que estan inactivos y estado
    // de la operacion en LOGICAL_DELETE_COMPLETED
    void deleteAllBoardsByStatusFalseAndWorkspacesByStatusFalse();

    // Debe borrar todas las operaciones con estado COMPLETED
    // Se tiene en cuenta que ya se ejecutaron las operaciones de tipo
    // LOGICAL_DELETE_COMPLETED
    void deleteAllCompletedOperations();
}
