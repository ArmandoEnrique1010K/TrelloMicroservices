package com.trello.workflow.note.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Note;
import com.trello.workflow.note.dto.response.UserNoteResponse;

@Mapper(componentModel = "spring")
public interface UserNoteResponseMapper {
    @Mappings({
            @Mapping(target = "createdByUser", ignore = true)
    })
    UserNoteResponse noteToUserNoteResponse(Note source);

    List<UserNoteResponse> noteListToUserNoteResponseList(List<Note> source);
}
