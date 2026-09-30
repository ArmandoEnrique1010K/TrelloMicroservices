package com.trello.workflow.task.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Task;
import com.trello.workflow.task.dto.response.AuthorTaskResponse;

@Mapper(componentModel = "spring")
public interface AuthorTaskResponseMapper {

    @Mappings({
            @Mapping(target = "author", ignore = true),
    })
    AuthorTaskResponse taskToAuthorTaskResponse(Task source);

    List<AuthorTaskResponse> taskListToAuthorTaskResponse(List<Task> source);
}
