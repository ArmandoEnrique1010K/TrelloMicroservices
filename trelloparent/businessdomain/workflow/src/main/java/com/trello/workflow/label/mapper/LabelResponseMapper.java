package com.trello.workflow.label.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.Label;
import com.trello.workflow.label.dto.response.LabelResponse;

@Mapper(componentModel = "spring")
public interface LabelResponseMapper {
    @Mappings({
            @Mapping(target = "hex", source = "color.hex")
    })
    LabelResponse labelToLabelResponse(Label source);

    List<LabelResponse> labelListToLabelResponseList(List<Label> source);
}
