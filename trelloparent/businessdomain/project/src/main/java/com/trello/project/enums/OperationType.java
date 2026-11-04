package com.trello.project.enums;

public enum OperationType {
    // La operación ACCEPT_INVITATION puede ejecutar solo 1 de 2 operaciones en el
    // microservicio Workflow: agregar permiso de acceso de tablero o activar un
    // permiso de acceso de tablero existente
    ACCEPT_INVITATION,
    //
    CHANGE_ROLE_MEMBER,
    DEACTIVATE_MEMBER,
    CREATE_BOARD,
    DELETE_BOARD,
    DELETE_WORKSPACE
}
