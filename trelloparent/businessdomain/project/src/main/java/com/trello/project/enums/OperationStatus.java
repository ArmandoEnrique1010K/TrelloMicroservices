package com.trello.project.enums;

public enum OperationStatus {
    // Operación distribuida en proceso
    PROCESSING,

    // La operación distribuida ha termiado correctamente
    COMPLETED,

    // Se esta ejecutando una compensacion real
    COMPENSATION_PENDING,

    // La compensación se completo correctamente
    COMPENSATED,

    // Requiere revision manual
    MANUAL_REVIEW
}