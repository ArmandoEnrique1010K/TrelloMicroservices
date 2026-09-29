package com.trello.workflow.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trello.workflow.entities.Note;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByTaskId(UUID taskId);
}
