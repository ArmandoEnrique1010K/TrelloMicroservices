package com.trello.workflow.services;

import com.trello.workflow.repositories.LabelRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.entities.Label;
import com.trello.workflow.exception.LabelNotFoundException;

@Service
public class LabelWorkflowServiceImpl implements LabelWorkflowService {

    private final LabelRepository labelRepository;

    LabelWorkflowServiceImpl(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    @Override
    public Label saveLabel(Label label) {
        return labelRepository.save(label);
    }

    @Override
    public List<Label> findAllLabelsByBoardId(UUID boardId) {
        return labelRepository.findByBoardId(boardId);
    }

    @Override
    public Label findLabelById(UUID labelId) throws LabelNotFoundException {
        Label label = labelRepository.findById(labelId).orElseThrow(LabelNotFoundException::new);
        return label;
    }

    @Override
    public void deleteLabelById(UUID labelId) throws LabelNotFoundException {
        Label label = labelRepository.findById(labelId).orElseThrow(LabelNotFoundException::new);
        labelRepository.delete(label);
    }

    @Override
    public boolean existsLabelByBoardIdAndContent(UUID boardId, String content) {
        return labelRepository.existsByBoardIdAndContent(boardId, content);
    }

    @Override
    public boolean existsLabelByBoardIdAndContentExcludingId(UUID boardId, String content, UUID labelId) {
        return labelRepository.existsByBoardIdAndContentAndIdNot(boardId, content, labelId);
    }

}
