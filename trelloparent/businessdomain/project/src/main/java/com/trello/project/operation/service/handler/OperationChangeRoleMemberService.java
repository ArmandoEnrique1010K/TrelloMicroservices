package com.trello.project.operation.service.handler;

import java.util.UUID;

public interface OperationChangeRoleMemberService {
    void applyChangeRoleMember(UUID operationId, boolean automatic);

    void revertChangeRoleMember(UUID operationId);
}
