package com.trello.workflow.task.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.workflow.entities.Task;
import com.trello.workflow.task.dto.response.TaskResponse;

@Mapper(componentModel = "spring")
public interface TaskResponseMapper {

    TaskResponse taskToTaskResponse(Task source);

    List<TaskResponse> taskListToTaskResponseList(List<Task> source);
}
