package com.trello.workflow.task.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;

import com.trello.workflow.entities.Label;
import com.trello.workflow.entities.Task;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.task.dto.response.LabelTaskResponse;

@Mapper(componentModel = "spring")
public interface LabelTaskResponseMapper {

    LabelTaskResponse taskToLabelTaskResponse(Task source);

    List<LabelTaskResponse> taskListToLabelTaskResponseList(List<Task> source);

    // Mapear los labels
    default List<LabelResponse> mapLabels(Set<Label> labels) {
        if (labels == null) {
            return List.of();
        }

        return labels.stream()
                .map(label -> {
                    LabelResponse response = new LabelResponse();
                    response.setId(label.getId());
                    response.setContent(label.getContent());
                    response.setHex(label.getColor().getHex());

                    return response;
                })
                .toList();
    }
}
