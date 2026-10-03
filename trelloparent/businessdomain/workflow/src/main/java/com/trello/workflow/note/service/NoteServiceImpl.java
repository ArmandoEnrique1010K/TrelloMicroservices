package com.trello.workflow.note.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

import com.trello.workflow.boardaccess.utils.BoardAccessRoleUtils;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Note;
import com.trello.workflow.entities.Task;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.exception.MismatchedAuthorException;
import com.trello.workflow.note.dto.request.NoteRequest;
import com.trello.workflow.note.dto.response.NoteResponse;
import com.trello.workflow.note.mapper.NoteRequestMapper;
import com.trello.workflow.note.mapper.NoteResponseMapper;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.NoteWorkflowService;
import com.trello.workflow.services.TaskWorkflowService;

@Service
public class NoteServiceImpl implements NoteService {

    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final TaskWorkflowService taskWorkflowService;
    private final NoteRequestMapper noteRequestMapper;
    private final NoteResponseMapper noteResponseMapper;
    private final NoteWorkflowService noteWorkflowService;

    public NoteServiceImpl(BoardAccessWorkflowService boardAccessWorkflowService,
            TaskWorkflowService taskWorkflowService, NoteRequestMapper noteRequestMapper,
            NoteResponseMapper noteResponseMapper, NoteWorkflowService noteWorkflowService) {
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.taskWorkflowService = taskWorkflowService;
        this.noteRequestMapper = noteRequestMapper;
        this.noteResponseMapper = noteResponseMapper;
        this.noteWorkflowService = noteWorkflowService;
    }

    @Override
    public NoteResponse createNote(NoteRequest noteRequest, UUID taskId, UUID userId)
            throws ForbiddenOperationException {

        // Obtener el ID del tablero
        Task task = taskWorkflowService.findTaskById(taskId);
        UUID boardId = task.getBoard().getId();

        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);
        Role role = boardAccess.getRole();
        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.MEMBER)) {
            throw new ForbiddenOperationException();
        }

        // Agrega una nota
        Note noteToNoteRequest = noteRequestMapper.noteRequestToNote(noteRequest);
        noteToNoteRequest.setCreatedByUserId(userId);
        noteToNoteRequest.setTask(task);
        Note savedNote = noteWorkflowService.saveNote(noteToNoteRequest);

        NoteResponse noteResponse = noteResponseMapper.noteToNoteResponse(savedNote);
        return noteResponse;
    }

    // Solamente el usuario que ha creado la nota la puede eliminar, además debe
    // tener el rol de MEMBER para eliminarlo (estricto)
    @Override
    public void deleteNote(UUID noteId, UUID userId) throws ForbiddenOperationException, MismatchedAuthorException {

        Note findedNote = noteWorkflowService.findNoteById(noteId);
        UUID boardId = findedNote.getTask().getBoard().getId();

        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

        // Comprueba de que el autor de la nota sea el mismo que el usuario que ha
        // iniciado sesion
        boolean isAuthor = findedNote.getCreatedByUserId().equals(userId);

        if (!isAuthor) {
            throw new MismatchedAuthorException();
        }

        // Comprueba que tenga el rol de Member
        Role role = boardAccess.getRole();

        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.MEMBER)) {
            throw new ForbiddenOperationException();
        }

        // Borra una nota por la entidad
        noteWorkflowService.deleteNoteByEntity(findedNote);
    }
}
