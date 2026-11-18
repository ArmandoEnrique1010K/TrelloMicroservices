package com.trello.workflow.operation.service.executor;

public interface OperationExecutorService {
    // Borrar fisicamente los tableros por estado false y fecha de borrado que
    // sobrepasa los 5 minutos
    void deleteAllBoardsByStatusFalseAndDeletedAtMore5minutes();

    // Borrar operaciones completadas
    void deleteAllCompletedOperations();

    // Borrar operaciones canceladas
    void deleteAllCanceledOperations();
}
