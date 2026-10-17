package com.trello.workflow.operation.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.workflow.boardaccess.exception.BoardAccessAlreadyExistsException;
import com.trello.workflow.entities.Board;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Operation;
import com.trello.workflow.enums.OperationStatus;
import com.trello.workflow.enums.OperationType;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.BusinessRuleException;
import com.trello.workflow.operation.exception.OperationNotFoundException;
import com.trello.workflow.operation.exception.OperationNotPendingException;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.BoardWorkflowService;
import com.trello.workflow.services.OperationWorkflowService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationAddBoardAccessServiceImpl implements OperationAddBoardAccessService {

    private final OperationWorkflowService operationWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final BoardWorkflowService boardWorkflowService;

    public OperationAddBoardAccessServiceImpl(OperationWorkflowService operationWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService,
            BoardWorkflowService boardWorkflowService) {
        this.operationWorkflowService = operationWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.boardWorkflowService = boardWorkflowService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyAddBoardAccess(Operation operation) throws BoardAccessAlreadyExistsException {
        Operation currentOperation = operationWorkflowService
                .findOperationById(operation.getId())
                .orElseThrow(OperationNotFoundException::new);

        // Aumenta el numero de intentos en 1
        currentOperation.setStatus(OperationStatus.PROCESSING);
        currentOperation.setAttempts(currentOperation.getAttempts() + 1);
        operationWorkflowService.saveOperation(currentOperation);

        // Toma los datos de la operación
        UUID boardId = currentOperation.getBoardId();
        UUID userId = currentOperation.getUserId();

        // Rol nuevo
        Role newRole = currentOperation.getNewRole();

        // Buscar por tablero
        Board board = boardWorkflowService.findBoardById(boardId);

        // Volver a preguntar si existe permiso de acceso
        if (boardAccessWorkflowService.existsBoardAccessByBoardIdAndUserId(boardId, userId)) {
            throw new BoardAccessAlreadyExistsException();
        }

        // Añadir el nuevo permiso de acceso
        BoardAccess savedBoardAccess = new BoardAccess();
        savedBoardAccess.setUserId(userId);
        savedBoardAccess.setRole(newRole);
        savedBoardAccess.setUserActive(true);
        savedBoardAccess.setBoard(board);

        boardAccessWorkflowService.saveBoardAccess(savedBoardAccess);

        // Marcar la operación como completada
        currentOperation.setStatus(OperationStatus.COMPLETED);
        currentOperation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(currentOperation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revertAddBoardAccess(UUID operationId) {
        Operation operation = operationWorkflowService
                .findOperationById(operationId)
                .orElseThrow(OperationNotFoundException::new);

        log.info("REVIRTIENDO CAMBIOS");
        if (operation.getType() != OperationType.ADD_BOARD_ACCESS) {
            throw new BusinessRuleException();
        }

        // IMPRIMIO COMPLETED
        // PERO COMO EL MICROSERVICIO SE HA DEMORADO, SE VA A CAMBIAR EL ESTADO
        // EN RESUMEN, EL ESTADO NO ES IMPORTANTE PARA REVERTIR LOS CAMBIOS

        log.info("ESTADO: " + operation.getStatus());
        // Solo se puede compensar una operación cuyo estado sea:
        // Permitido: PROCESSING, PENDING, COMPLETED
        // No permitido: MANUAL_REVIEW,CREATED
        if (operation.getStatus() == OperationStatus.MANUAL_REVIEW
                && operation.getStatus() == OperationStatus.CREATED) {

            // TODO: CORREGIR LA EXCEPCIÓN
            throw new OperationNotPendingException();
        }

        log.info("PROCESANDO...");
        operation.setStatus(OperationStatus.PROCESSING);
        operation.setCompletedAt(null);
        operationWorkflowService.saveOperation(operation);

        try {
            // Elimina únicamente el acceso asociado a esta operación.
            log.info("ELIMINANDO PERMISO DE ACCESO");

            boardAccessWorkflowService.deleteBoardAccessByBoardIdAndUserId(operation.getBoardId(),
                    operation.getUserId());

            // Solamente existe la BoardAccessNotFoundException, pero todas las excepciones
            // se tratan como runtime exception
        } catch (RuntimeException exception) {
            /*
             * Si la consulta es estrictamente por operationId,
             * la ausencia del efecto puede tratarse como idempotencia:
             * el acceso ya no existe o nunca llegó a crearse.
             *
             * Esto es válido únicamente si se ha verificado que no
             * hay otros efectos pendientes de compensar.
             */
            log.info("SE SOLICITA REVISION MANUAL");

            operation.setStatus(OperationStatus.MANUAL_REVIEW);
            operation.setCompletedAt(null);
            operationWorkflowService.saveOperation(operation);

        } catch (Exception ex) {
            // No asumir que cualquier excepción es un error permanente.
            // Registrar el error y propagarlo para evitar confirmar
            // silenciosamente una transacción potencialmente inconsistente.
            throw ex;
        }

        // La compensación ha finalizado correctamente.
        operation.setStatus(OperationStatus.PENDING);
        operation.setCompletedAt(LocalDateTime.now());
        operationWorkflowService.saveOperation(operation);

        log.info("SE SOLICITA REINTENTAR LA OPERACION");
    }

}
