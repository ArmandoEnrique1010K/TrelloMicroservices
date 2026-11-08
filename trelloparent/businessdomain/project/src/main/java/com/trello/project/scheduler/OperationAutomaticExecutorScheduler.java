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

    // Por cada 30 segundos se ejecutaran operaciones pendientes
    // Hay una diferencia entre el uso de fixedRate y fixedDelay:

    // fixedRate: ejecuta por cada 30 segundos, contando desde el inicio de la
    // ejecución anterior
    // fixedDelay: ejecuta por cada 30 segundos, comenzando despues de finalizar la
    // ejecución anterior
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void deleteAcceptedInvitations() {
        log.info("EJECUTANDO LAS OPERACIONES PENDIENTES");

        // Ejecutar todas las operaciones pendientes
        operationExecutorService.executeAllCompensatedOperations();
    }
}
