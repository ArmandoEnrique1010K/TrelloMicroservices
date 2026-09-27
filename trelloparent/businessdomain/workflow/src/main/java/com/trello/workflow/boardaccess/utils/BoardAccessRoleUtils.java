package com.trello.workflow.boardaccess.utils;

import com.trello.workflow.enums.Role;

public class BoardAccessRoleUtils {
    private BoardAccessRoleUtils() {
    }

    // Permite una operación si el rol del usuario tiene un nivel de autorización
    // suficiente.
    // OWNER > ADMIN > MEMBER > VIEW
    public static boolean hasAuthorization(Role hisRole, Role permitRole) {
        if (hisRole == null || permitRole == null) {
            return false;
        }

        return getRoleLevel(hisRole) <= getRoleLevel(permitRole);

    }

    private static int getRoleLevel(Role role) {

        return switch (role) {
            case OWNER -> 1;
            case ADMIN -> 2;
            case MEMBER -> 3;
            case VIEWER -> 4;
        };
    }

}
