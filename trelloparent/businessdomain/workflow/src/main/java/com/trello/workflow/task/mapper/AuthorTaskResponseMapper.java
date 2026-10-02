package com.trello.workflow.task.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Label;
import com.trello.workflow.entities.Task;
import com.trello.workflow.label.dto.response.LabelColorResponse;
import com.trello.workflow.task.dto.response.AuthorTaskResponse;

@Mapper(componentModel = "spring")
public interface AuthorTaskResponseMapper {

    @Mappings({
            @Mapping(target = "author", ignore = true)
            // @Mapping(target = "labels", ignore = true)
    })
    AuthorTaskResponse taskToAuthorTaskResponse(Task source);

    List<AuthorTaskResponse> taskListToAuthorTaskResponse(List<Task> source);

    default List<LabelColorResponse> mapLabels(Set<Label> labels) {
        if (labels == null) {
            return List.of();
        }

        return labels.stream()
                .map(label -> {
                    LabelColorResponse response = new LabelColorResponse();
                    response.setContent(label.getContent());
                    response.setHex(label.getColor().getHex());

                    return response;
                })
                .toList();
    }
}
