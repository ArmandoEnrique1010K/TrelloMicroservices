package com.trello.workflow.task.mapper;

import org.mapstruct.Mapper;

import com.trello.workflow.entities.Task;
import com.trello.workflow.task.dto.response.TaskResponse;

@Mapper(componentModel = "spring")
public interface TaskResponseMapper {

    TaskResponse taskToTaskResponse(Task source);
}
