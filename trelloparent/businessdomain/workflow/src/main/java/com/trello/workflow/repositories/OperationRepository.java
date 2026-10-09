package com.trello.workflow.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.Operation;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

}
