package com.trello.workflow.note.service;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.exception.MismatchedAuthorException;
import com.trello.workflow.note.dto.request.NoteRequest;
import com.trello.workflow.note.dto.response.NoteResponse;
import com.trello.workflow.note.dto.response.UserNoteResponse;

public interface NoteService {

    NoteResponse createNote(NoteRequest noteRequest, UUID taskId, UUID userId) throws ForbiddenOperationException;

    // TODO: ELIMINAR ESTE SERVICIO
    List<UserNoteResponse> listAllNotesByTaskId(UUID taskId, UUID userId);

    void deleteNote(UUID noteId, UUID userId) throws ForbiddenOperationException, MismatchedAuthorException;
}
