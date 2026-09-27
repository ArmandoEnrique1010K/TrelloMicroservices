package com.trello.workflow.services;

import com.trello.workflow.repositories.HistoryRepository;
import org.springframework.stereotype.Service;

import com.trello.workflow.entities.History;

@Service
public class HistoryWorkflowServiceImpl implements HistoryWorkflowService {

    private final HistoryRepository historyRepository;

    public HistoryWorkflowServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public History saveHistory(History history) {
        return historyRepository.save(history);
    }
}
