package com.trello.workflow.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.History;

public interface HistoryRepository extends JpaRepository<History, UUID> {

}
