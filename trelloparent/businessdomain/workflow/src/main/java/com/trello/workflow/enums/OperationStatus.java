package com.trello.workflow.enums;

public enum OperationStatus {
    // La operación se registró en Workflow, pero aún no se ha ejecutado.
    CREATED,

    // La operación está pendiente de ejecución o de un nuevo intento.
    PENDING,

    // Workflow está ejecutando la operación original.
    PROCESSING,

    // La operación original terminó correctamente.
    COMPLETED,

    // La operación quedó en un estado que no puede resolverse automáticamente con
    // seguridad.
    // TODO: ELIMINAR ESTE ESTADO Y TAMBIEN EN CADA UNO DE LOS HANDLERES DEL
    // SERVICIO DE OPERACIONES
    MANUAL_REVIEW
}
