package com.trello.workflow.note.service;

import java.util.UUID;

import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.exception.MismatchedAuthorException;
import com.trello.workflow.note.dto.request.NoteRequest;
import com.trello.workflow.note.dto.response.NoteResponse;

public interface NoteService {

    NoteResponse createNote(NoteRequest noteRequest, UUID taskId, UUID userId) throws ForbiddenOperationException;

    void deleteNote(UUID noteId, UUID userId) throws ForbiddenOperationException, MismatchedAuthorException;
}
