package com.trello.project.client.fallback;

import java.util.UUID;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.trello.project.client.WorkflowOperationCommandClient;
import com.trello.project.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkflowOperationCommandClientFallbackFactory implements FallbackFactory<WorkflowOperationCommandClient> {
    @Override
    public WorkflowOperationCommandClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para WorkflowOperationCommandClient. Causa: {}",
                cause.toString(),
                cause);
        return new WorkflowOperationCommandClient() {

            @Override
            public ResponseEntity<Void> revertAddBoardAccess(UUID operationId) {
                log.error("Error relacionado con revertAddBoardAccess");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> revertActivateBoardAccess(UUID operationId) {
                log.error("Error relacionado con revertActivateBoardAccess");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> revertChangeRoleBoardAccess(UUID operationId) {
                log.error("Error relacionado con revertChangeRoleBoardAccess");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> revertDeactivateBoardAccess(UUID operationId) {
                log.error("Error relacionado con revertDeactivateBoardAccess");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> revertAddBoardAndBoardAccessUserOwner(UUID operationId) {
                log.error("Error relacionado con revertAddBoardAndBoardAccessUserOwner");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> revertDeleteBoardAndBoardAccessAndTasks(UUID operationId) {
                log.error("Error relacionado con revertDeleteBoardAndBoardAccessAndTasks");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> revertDeleteAllBoardsAndBoardAccessAndTasks(UUID operationId) {
                log.error("Error relacionado con revertDeleteAllBoardsAndBoardAccessAndTasks");
                throw new ServiceUnavailableException();
            }
        };
    }
}
