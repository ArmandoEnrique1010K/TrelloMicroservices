package com.trello.workflow.services;

import com.trello.workflow.repositories.HistoryRepository;

import java.util.List;
import java.util.UUID;

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

    @Override
    public List<History> findAllHistoriesByTaskId(UUID taskId) {
        return historyRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
}
