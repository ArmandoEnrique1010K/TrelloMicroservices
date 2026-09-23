package com.trello.workflow.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.BoardAccess;

// TODO: GUARDAR INFORMACIÓN DEL USUARIO CUANDO CREA UN TABLERO
// PETICION PROJECT -> FEIGN -> TASK, EL MICROSERVICIO PROJECT DEBE EJECUTAR UNA PETICION EN TASK PARA ALMACENAR LOS DATOS
public interface BoardAccessRepository extends JpaRepository<BoardAccess, UUID> {

}
