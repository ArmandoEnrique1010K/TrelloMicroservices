package com.trello.workflow.services;

import com.trello.workflow.repositories.NoteRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Note;
import com.trello.workflow.exception.NoteNotFoundException;

@Service
public class NoteWorkflowServiceImpl implements NoteWorkflowService {

    private final NoteRepository noteRepository;

    NoteWorkflowServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    @Override
    public List<Note> findAllNotesByTaskId(UUID taskId) {
        return noteRepository.findByTaskId(taskId);
    }

    @Override
    public void deleteNoteByEntity(Note note) {
        noteRepository.delete(note);
    }

    @Override
    public Note findNoteById(UUID noteId) throws NoteNotFoundException {
        Note note = noteRepository.findById(noteId).orElseThrow(NoteNotFoundException::new);
        return note;
    }
}
