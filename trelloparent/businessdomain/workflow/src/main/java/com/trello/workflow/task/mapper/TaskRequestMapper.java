package com.trello.workflow.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Task;
import com.trello.workflow.task.dto.request.TaskRequest;

@Mapper(componentModel = "spring")
public interface TaskRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "board", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "labels", ignore = true),
            @Mapping(target = "notes", ignore = true),
            @Mapping(target = "histories", ignore = true)
    })
    Task taskRequestToTask(TaskRequest source);
}
