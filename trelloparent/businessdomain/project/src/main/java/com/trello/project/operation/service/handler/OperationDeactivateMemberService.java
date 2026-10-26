package com.trello.project.operation.service.handler;

import java.util.UUID;

public interface OperationDeactivateMemberService {
    void applyDeactivateMember(UUID operationId, boolean automatic);

    void revertDeactivateMember(UUID operationId);
}
