package com.trello.workflow.services;

import java.util.UUID;

import com.trello.workflow.entities.Note;
import com.trello.workflow.exception.NoteNotFoundException;

public interface NoteWorkflowService {
    Note saveNote(Note note);

    Note findNoteById(UUID noteId)
            throws NoteNotFoundException;

    void deleteNoteByEntity(Note note);
}
