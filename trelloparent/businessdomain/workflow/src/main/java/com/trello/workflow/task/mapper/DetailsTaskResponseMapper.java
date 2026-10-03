package com.trello.workflow.task.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Label;
import com.trello.workflow.entities.Note;
import com.trello.workflow.entities.Task;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.note.dto.response.UserNoteResponse;
import com.trello.workflow.task.dto.response.DetailsTaskResponse;

@Mapper(componentModel = "spring")
public interface DetailsTaskResponseMapper {

    // Se ignoran esos campos que se encuentran en DetailsTaskResponse
    @Mappings({
            @Mapping(target = "createdByUser", ignore = true),
            @Mapping(target = "histories", ignore = true)
    })
    DetailsTaskResponse taskToDetailsTaskResponse(Task source);

    // MapStruct puede utilizar este método para transformar automáticamente el
    // campo 'Task.labels' de 'Set<Label>' a 'List<LabelResponse>'
    default List<LabelResponse> mapLabels(Set<Label> labels) {
        if (labels == null) {
            return List.of();
        }

        return labels.stream()
                .map(label -> {
                    LabelResponse response = new LabelResponse();
                    response.setId(label.getId());
                    response.setContent(label.getContent());
                    // Color es un enum que contiene el código hexadecimal asociado al color.
                    response.setHex(label.getColor().getHex());
                    return response;
                })
                .toList();
    }

    // MapStruct utiliza este método para transformar las notas almacenadas
    default List<UserNoteResponse> mapNotes(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            return List.of();
        }

        return notes.stream()
                .map(note -> {
                    UserNoteResponse response = new UserNoteResponse();
                    response.setId(note.getId());
                    response.setContent(note.getContent());
                    response.setCreatedAt(note.getCreatedAt());

                    // No se establece createdByUser aquí. Este campo requiere una llamada al
                    // microservicio Identity, por lo que el enriquecimiento se realiza
                    // posteriormente desde TaskServiceImpl.
                    return response;
                })
                .toList();
    }

}
