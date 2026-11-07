package com.trello.workflow.services;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.entities.History;

public interface HistoryWorkflowService {
    History saveHistory(History history);

    List<History> findAllHistoriesByTaskId(UUID taskId);
}
