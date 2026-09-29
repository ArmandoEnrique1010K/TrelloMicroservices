package com.trello.workflow.services;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.entities.Note;
import com.trello.workflow.exception.NoteNotFoundException;

public interface NoteWorkflowService {
    Note saveNote(Note note);

    List<Note> findAllNotesByTaskId(UUID taskId);

    Note findNoteById(UUID noteId) throws NoteNotFoundException;

    void deleteNoteByEntity(Note note);
}
