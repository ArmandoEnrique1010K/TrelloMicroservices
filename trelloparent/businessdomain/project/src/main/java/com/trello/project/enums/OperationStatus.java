package com.trello.project.enums;

public enum OperationStatus {
    // La operación está registrada, pero aún no se han aplicado
    // los cambios locales en Project.
    CREATED,

    // Los cambios locales están aplicados y la operación
    // está lista para enviarse a Workflow.
    PENDING,

    // Se está procesando la operación distribuida.
    PROCESSING,

    // La operación distribuida terminó correctamente.
    COMPLETED,

    // Se decidió compensar, pero todavía no ha terminado.
    COMPENSATION_PENDING,

    // Un proceso está ejecutando la compensación.
    COMPENSATION_PROCESSING,

    // Toda la compensación distribuida terminó correctamente.
    COMPENSATED,

    // Se requiere intervención manual.
    MANUAL_REVIEW

    // Los estados de PEDING y COMPENSATION_PENDING indican una operación que ha
    // quedado pendiente
}