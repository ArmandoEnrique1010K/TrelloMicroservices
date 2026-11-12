package com.trello.project.client.fallback;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.trello.project.client.WorkflowBoardCommandClient;
import com.trello.project.exception.ServiceUnavailableException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkflowBoardCommandClientFallbackFactory implements FallbackFactory<WorkflowBoardCommandClient> {
    @Override
    public WorkflowBoardCommandClient create(Throwable cause) {
        log.error(
                "Fallback ejecutado para WorkflowBoardCommandClient. Causa: {}",
                cause.toString(),
                cause);

        return new WorkflowBoardCommandClient() {

            @Override
            public ResponseEntity<Void> addBoardAndBoardAccessUserOwner(UUID boardId, UUID userId, UUID operationId) {
                log.error("Error relacionado con addBoardAndBoardAccessUserOwner");

                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> deleteBoardAndBoardAccesses(UUID boardId, UUID operationId) {
                log.error("Error relacionado con deleteBoardAndBoardAccesses");

                throw new ServiceUnavailableException();
            }

            @Override
            public ResponseEntity<Void> deleteAllBoardsByIdsAndMultipleBoardAccesses(List<UUID> boardIds,
                    UUID operationId) {
                log.error("Error relacionado con deleteAllBoardsByIdsAndMultipleBoardAccesses");

                throw new ServiceUnavailableException();
            }

        };
    }

}
