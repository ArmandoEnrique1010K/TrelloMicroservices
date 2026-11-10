package com.trello.project.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trello.project.operation.service.executor.OperationExecutorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationAutomaticExecutorScheduler {
    private final OperationExecutorService operationExecutorService;

    // PROBLEMA: AUTENTICACIÓN EN LA EJECUCIÓN AUTOMÁTICA DE OPERACIONES SAGA
    //
    // Al ejecutar las operaciones pendientes mediante el Scheduler, se producían
    // errores HTTP 401 (Unauthorized) en las llamadas Feign realizadas desde los
    // procesadores.
    //
    // La causa era que el RequestInterceptor dependía del contexto HTTP actual
    // para recuperar el JWT del usuario autenticado. Sin embargo, las tareas
    // programadas se ejecutan fuera del ciclo de vida de una petición HTTP,
    // por lo que no existe un token de usuario que pueda propagarse.
    //
    // Inicialmente se consideró pasar el JWT del usuario a cada procesador,
    // pero esta solución acoplaría la ejecución de las operaciones SAGA a la
    // sesión del usuario que las originó.
    //
    // Por ello, se debe separar la autenticación del usuario de la autenticación
    // entre microservicios, utilizando un JWT de servicio para las comunicaciones
    // internas entre Project y Workflow.

    //

    // Por cada 30 segundos se ejecutaran operaciones pendientes
    // Hay una diferencia entre el uso de fixedRate y fixedDelay:

    // fixedRate: ejecuta por cada 30 segundos, contando desde el inicio de la
    // ejecución anterior
    // fixedDelay: ejecuta por cada 30 segundos, comenzando despues de finalizar la
    // ejecución anterior
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void executeCompensatedOperations() {
        log.info("EJECUTANDO LAS OPERACIONES PENDIENTES");

        // Ejecutar todas las operaciones pendientes
        operationExecutorService.executeAllCompensatedOperations();
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void deleteBoardsAndWorkspacesInDatabase() {
        log.info("ELIMINANDO TABLEROS Y ESPACIOS DE TRABAJO INACTIVOS");

        operationExecutorService.deleteAllBoardsByStatusFalseAndWorkspacesByStatusFalse();
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void deleteCompletedOperations() {
        log.info("ELIMINANDO OPERACIONES COMPLETADAS");

        operationExecutorService.deleteAllCompletedOperations();
    }

}
