package com.trello.project.enums;

public enum CleanupStatus {
    // Limpieza pendiente
    PENDING,

    // Procesando limpieza en Project
    EXECUTION_PENDING,

    // Procesando limpieza en Workflow
    EXECUTING,

    // Limpieza completada
    COMPLETED
}
