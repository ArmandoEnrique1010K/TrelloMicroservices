package com.trello.workflow.label.service;

import java.util.List;
import java.util.UUID;

import com.trello.workflow.enums.Color;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.label.dto.request.LabelRequest;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.label.exception.LabelAlreadyExistsException;

public interface LabelService {

    LabelResponse createLabel(LabelRequest labelRequest, UUID boardId, Color colorName, UUID userId)
            throws ForbiddenOperationException, LabelAlreadyExistsException;

    List<LabelResponse> listAllLabels(UUID boardId, UUID userId)
            throws ForbiddenOperationException;

    LabelResponse editLabel(UUID labelId, LabelRequest labelRequest, Color colorName, UUID userId)
            throws ForbiddenOperationException, LabelAlreadyExistsException;

    void deleteLabel(UUID labelId, UUID userId)
            throws ForbiddenOperationException;
}
