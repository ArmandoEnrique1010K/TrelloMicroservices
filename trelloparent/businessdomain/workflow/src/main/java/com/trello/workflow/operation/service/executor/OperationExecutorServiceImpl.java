package com.trello.workflow.operation.service.executor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.HistoryWorkflowService;
import com.trello.workflow.services.LabelWorkflowService;
import com.trello.workflow.services.NoteWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;
import com.trello.workflow.services.TaskWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationExecutorServiceImpl implements OperationExecutorService {
    private final OperationWorkflowService operationWorkflowService;
    private final BoardWorkflowService boardWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final TaskWorkflowService taskWorkflowService;
    private final LabelWorkflowService labelWorkflowService;
    private final NoteWorkflowService noteWorkflowService;
    private final HistoryWorkflowService historyWorkflowService;

    public OperationExecutorServiceImpl(
            OperationWorkflowService operationWorkflowService,
            BoardWorkflowService boardWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService,
            TaskWorkflowService taskWorkflowService,
            LabelWorkflowService labelWorkflowService,
            NoteWorkflowService noteWorkflowService,
            HistoryWorkflowService historyWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.taskWorkflowService = taskWorkflowService;
        this.labelWorkflowService = labelWorkflowService;
        this.noteWorkflowService = noteWorkflowService;
        this.historyWorkflowService = historyWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAllBoardsByStatusFalseAndDeletedAtMore5minutes() {
        LocalDateTime expirationDate = LocalDateTime.now().minusMinutes(5);

        // A diferencia del microservicio Project en el cual se utilizaba un bucle for
        // para eliminar fisicamente uno por uno cada "board" en la base de datos, en
        // este caso se utilizara un metodo de eliminación masiva a la base de datos por
        // cada una de las entidades relacionadas

        // Eliminar relaciones y entidades dependientes
        boardAccessWorkflowService.deleteAllBoardsAccessByBoardStatusFalseAndBoardDeleteAtBefore(expirationDate);

        // Eliminar tareas y sus entidades dependientes

        // Eliminar notas
        noteWorkflowService
                .deleteAllNotesByBoardStatusFalseAndBoardDeleteAtBefore(expirationDate);

        // Eliminar historiales
        historyWorkflowService
                .deleteAllHistoriesByBoardStatusFalseAndBoardDeleteAtBefore(expirationDate);

        // Eliminar relaciones entre tareas y etiquetas
        taskWorkflowService
                .deleteAllTaskLabelRelationsByBoardStatusFalseAndBoardDeletedAtBefore(
                        expirationDate);

        // Eliminar tareas
        taskWorkflowService
                .deleteAllTasksByBoardStatusFalseAndBoardDeleteAtBefore(expirationDate);

        // Eliminar etiquetas
        labelWorkflowService
                .deleteAllLabelsByBoardStatusFalseAndBoardDeleteAtBefore(expirationDate);

        // Finalmente, eliminar los tableros
        boardWorkflowService.deleteAllBoardsByStatusFalseAndDeleteAtBefore(expirationDate);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAllCompletedOperations() {
        operationWorkflowService.deleteAllOperationsByStatus(OperationStatus.COMPLETED);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAllCanceledOperations() {
        operationWorkflowService.deleteAllOperationsByStatus(OperationStatus.CANCELED);
    }

}
