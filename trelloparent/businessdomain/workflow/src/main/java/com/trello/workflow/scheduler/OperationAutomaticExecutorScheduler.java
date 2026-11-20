package com.trello.workflow.scheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trello.workflow.operation.service.executor.OperationExecutorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationAutomaticExecutorScheduler {
    private final OperationExecutorService operationExecutorService;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void deleteBoardsInDatabase() {
        log.info("ELIMINANDO TABLEROS INACTIVOS");

        operationExecutorService.deleteAllBoardsByStatusFalseAndDeletedAtMore5minutes();
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void deleteCompletedOperations() {
        log.info("ELIMINANDO OPERACIONES COMPLETADAS");
        operationExecutorService.deleteAllCompletedOperations();
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void deleteCanceledOperations() {
        log.info("ELIMINANDO OPERACIONES CANCELADAS");
        operationExecutorService.deleteAllCanceledOperations();
    }
}
