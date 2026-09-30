package com.trello.workflow.label.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.trello.workflow.entities.Label;
import com.trello.workflow.label.dto.response.LabelResponse;

@Mapper(componentModel = "spring")
public interface LabelResponseMapper {

    LabelResponse labelToLabelResponse(Label source);

    List<LabelResponse> labelListToLabelResponseList(List<Label> source);
}
