package com.trello.project.client.fallback;

import java.util.UUID;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.trello.project.client.WorkflowBoardAccessCommandClient;
import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkflowBoardAccessCommandClientFallbackFactory
        implements FallbackFactory<WorkflowBoardAccessCommandClient> {
    @Override
    public WorkflowBoardAccessCommandClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para WorkflowBoardAccessCommandClient. Causa: {}",
                cause.toString(),
                cause);

        return new WorkflowBoardAccessCommandClient() {

            @Override
            public ResponseEntity<Void> addBoardAccess(UUID boardId, WorkflowRole roleName, UUID operationId) {
                log.error("Error relacionado con addBoardAccess");

                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> activateBoardAccess(UUID boardId, UUID memberUserId,
                    WorkflowRole roleName, UUID operationId) {
                log.error("Error relacionado con activateBoardAccess");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> changeRoleBoardAccess(UUID boardId, UUID memberUserId,
                    WorkflowRole roleName) {
                log.error("Error relacionado con changeRoleBoardAccess");
                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> deactivateBoardAccess(UUID boardId, UUID memberUserId) {
                log.error("Error relacionado con deactivateBoardAccess");
                throw new ServiceUnavailableException();
            }

        };
    }
}
