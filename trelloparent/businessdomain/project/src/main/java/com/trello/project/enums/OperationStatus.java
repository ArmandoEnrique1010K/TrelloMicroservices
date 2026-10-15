package com.trello.project.enums;

public enum OperationStatus {
    // Operación registrada; aún no se aplicaron cambios locales
    CREATED,

    // Cambios locales aplicados; falta ejecutar la acción remota
    PENDING,

    // Ejecución de la acción remota en curso
    PROCESSING,

    // Operación completada (incluye si una operación ya no esta compensada)
    COMPLETED,

    // Compensación pendiente de completar
    COMPENSATION_PENDING,

    // Compensación en curso
    COMPENSATION_PROCESSING,

    // Los cambios locales se revirtieron, pero falta completar o confirmar la
    // compensación distribuida
    COMPENSATED,

    // Ambos servicios compensados; se permite una nueva aceptación
    COMPENSATION_COMPLETED,

    // Existe una inconsistencia que requiere revisión
    MANUAL_REVIEW
}