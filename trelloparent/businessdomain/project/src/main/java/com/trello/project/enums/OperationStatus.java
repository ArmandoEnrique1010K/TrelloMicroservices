package com.trello.project.enums;

public enum OperationStatus {
    // Operación registrada; aún no se aplicaron cambios locales
    // La operación fue registrada en la base de datos, pero todavía no se han
    // aplicado los cambios de negocio.
    REGISTERED,

    // Cambios locales aplicados; falta ejecutar la acción remota
    // Los cambios locales fueron aplicados y la operación está pendiente de
    // ejecutarse o confirmarse en el servicio remoto.
    EXECUTION_PENDING,

    // Ejecución de la acción remota en curso
    // Se está intentando ejecutar la operación original, incluida la comunicación
    // con Workflow.
    EXECUTING,

    // Operación completada (incluye si una operación ya no esta compensada)
    // La operación original terminó correctamente en los servicios involucrados.
    COMPLETED,

    // Compensación pendiente de completar
    // Se determinó que es necesario revertir los cambios locales y todavía falta
    // completar esa reversión.
    COMPENSATION_PENDING,

    // Compensación en curso
    // Se están revirtiendo los cambios locales de Project.
    LOCAL_COMPENSATION_IN_PROGRESS,

    // Los cambios locales se revirtieron, pero falta completar o confirmar la
    // compensación distribuida
    // Project ya revirtió sus cambios, pero todavía falta completar la compensación
    // en Workflow.
    LOCAL_COMPENSATION_COMPLETED,

    // Ambos servicios compensados; se permite una nueva aceptación
    // La compensación terminó correctamente tanto en Project como en Workflow.
    COMPENSATION_COMPLETED,

    // Existe una inconsistencia que requiere revisión
    // La operación no puede continuar de forma segura mediante el flujo automático
    // y requiere revisión o intervención manual.
    MANUAL_INTERVENTION_REQUIRED
}