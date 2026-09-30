package com.trello.workflow.label.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Label;
import com.trello.workflow.label.dto.request.LabelRequest;

@Mapper(componentModel = "spring")
public interface LabelRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "board", ignore = true)
    })
    Label labelRequestToLabel(LabelRequest source);
}
