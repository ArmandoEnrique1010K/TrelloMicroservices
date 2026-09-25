package com.trello.project.client.utils;

import com.trello.project.client.enums.WorkflowRole;
import com.trello.project.enums.Role;

public class WorkflowRoleUtils {
    private WorkflowRoleUtils() {
    }

    // Método privado para parsear Role a WorkflowRole

    // Parsear rol (Role -> WorkflowRole). WorkflowRole tiene OWNER, pero no se
    // debe mapear desde Role.
    // WorkflowRole: OWNER, ADMIN, MEMBER, VIEWER
    // Role: ADMIN, MEMBER, VIEWER

    public static WorkflowRole parseRoleToWorkflowRole(Role role) {
        if (role == null) {
            return null;
        }

        return switch (role) {
            case ADMIN -> WorkflowRole.ADMIN;
            case MEMBER -> WorkflowRole.MEMBER;
            case VIEWER -> WorkflowRole.VIEWER;
            default -> throw new IllegalArgumentException("Rol desconocido: " + role);
        };
    }

}
