package com.trello.workflow.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByBoardId(UUID boardId);
}
