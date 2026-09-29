package com.trello.workflow.note.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Note;
import com.trello.workflow.note.dto.request.NoteRequest;

@Mapper(componentModel = "spring")
public interface NoteRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdByUserId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "task", ignore = true)
    })
    Note noteRequestToNote(NoteRequest source);
}
