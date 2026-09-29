package com.trello.workflow.note.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.workflow.entities.Note;
import com.trello.workflow.note.dto.response.NoteResponse;

@Mapper(componentModel = "spring")
public interface NoteResponseMapper {

    NoteResponse noteToNoteResponse(Note source);

    List<NoteResponse> noteListToNoteResponseList(List<Note> source);
}
