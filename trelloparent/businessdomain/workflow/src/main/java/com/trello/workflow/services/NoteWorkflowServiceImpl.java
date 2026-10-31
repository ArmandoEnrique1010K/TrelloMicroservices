package com.trello.workflow.services;

import com.trello.workflow.repositories.NoteRepository;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Note;
import com.trello.workflow.exception.NoteNotFoundException;

@Service
public class NoteWorkflowServiceImpl implements NoteWorkflowService {

    private final NoteRepository noteRepository;

    public NoteWorkflowServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    @Override
    public Note findNoteById(UUID noteId)
            throws NoteNotFoundException {
        Note note = noteRepository.findById(noteId).orElseThrow(NoteNotFoundException::new);
        return note;
    }

    @Override
    public void deleteNoteByEntity(Note note) {
        noteRepository.delete(note);
    }
}
