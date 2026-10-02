package com.trello.workflow.history.mapper;

import java.util.List;

import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.entities.History;
import com.trello.workflow.history.dto.response.HistoryResponse;

public interface HistoryResponseMapper {
    @Mappings({
            @Mapping(target = "createdByUser", ignore = true)
    })
    HistoryResponse historyToHistoryResponse(History source);

    List<HistoryResponse> historyListToHistoryResponseList(List<History> source);
}
