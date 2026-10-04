package com.trello.workflow.services;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.entities.Label;
import com.trello.workflow.exception.LabelNotFoundException;

public interface LabelWorkflowService {

    Label saveLabel(Label label);

    List<Label> findAllLabelsByBoardId(UUID boardId);

    Label findLabelById(UUID labelId) throws LabelNotFoundException;

    void deleteLabelById(UUID labelId) throws LabelNotFoundException;

    boolean existsLabelByBoardIdAndContent(UUID boardId, String content);

    boolean existsLabelByBoardIdAndContentExcludingId(UUID boardId, String content, UUID labelId);

    Label findLabelByIdAndBoardId(UUID labelId, UUID boardId) throws LabelNotFoundException;
}
