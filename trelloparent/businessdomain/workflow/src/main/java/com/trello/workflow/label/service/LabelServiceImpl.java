package com.trello.workflow.label.service;

import com.trello.workflow.services.LabelWorkflowServiceImpl;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trello.workflow.boardaccess.utils.BoardAccessRoleUtils;
import com.trello.workflow.entities.Board;
import com.trello.workflow.entities.BoardAccess;
import com.trello.workflow.entities.Label;
import com.trello.workflow.enums.Color;
import com.trello.workflow.enums.Role;
import com.trello.workflow.exception.ForbiddenOperationException;
import com.trello.workflow.label.dto.request.LabelRequest;
import com.trello.workflow.label.dto.response.LabelResponse;
import com.trello.workflow.label.exception.LabelAlreadyExistsException;
import com.trello.workflow.label.mapper.LabelRequestMapper;
import com.trello.workflow.label.mapper.LabelResponseMapper;
import com.trello.workflow.services.BoardAccessWorkflowService;
import com.trello.workflow.services.LabelWorkflowService;

@Service
public class LabelServiceImpl implements LabelService {

    private final LabelWorkflowServiceImpl labelWorkflowServiceImpl;
    private final LabelWorkflowService labelWorkflowService;
    private final BoardAccessWorkflowService boardAccessWorkflowService;
    private final LabelRequestMapper labelRequestMapper;
    private final LabelResponseMapper labelResponseMapper;

    public LabelServiceImpl(LabelWorkflowService labelWorkflowService,
            BoardAccessWorkflowService boardAccessWorkflowService, LabelRequestMapper labelRequestMapper,
            LabelResponseMapper labelResponseMapper, LabelWorkflowServiceImpl labelWorkflowServiceImpl) {
        this.labelWorkflowService = labelWorkflowService;
        this.boardAccessWorkflowService = boardAccessWorkflowService;
        this.labelRequestMapper = labelRequestMapper;
        this.labelResponseMapper = labelResponseMapper;
        this.labelWorkflowServiceImpl = labelWorkflowServiceImpl;
    }

    @Override
    public LabelResponse createLabel(LabelRequest labelRequest, UUID boardId, UUID userId)
            throws LabelAlreadyExistsException, ForbiddenOperationException {

        String content = labelRequest.getContent();

        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

        Role role = boardAccess.getRole();

        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        if (labelWorkflowService.existsLabelByBoardIdAndContent(boardId, content)) {
            throw new LabelAlreadyExistsException();
        }

        Board board = boardAccess.getBoard();

        Label labelToLabelRequest = labelRequestMapper.labelRequestToLabel(labelRequest);
        labelToLabelRequest.setBoard(board);

        Label savedLabel = labelWorkflowService.saveLabel(labelToLabelRequest);
        LabelResponse labelResponse = labelResponseMapper.labelToLabelResponse(savedLabel);

        return labelResponse;
    }

    @Override
    public List<LabelResponse> listAllLabels(UUID boardId, UUID userId) throws ForbiddenOperationException {
        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

        Role role = boardAccess.getRole();

        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        List<Label> listLabelsByBoardId = labelWorkflowService.findAllLabelsByBoardId(boardId);
        return labelResponseMapper.labelListToLabelResponseList(listLabelsByBoardId);
    }

    // No se toma en cuenta el usuario quien fue el creador de la etiqueta,
    // cualquier miembro con el rol de ADMIN o el propietario lo puede editar

    // Como se va a modificar la etiqueta, las etiquetas de las tareas tambien se
    // modificaran
    @Override
    public LabelResponse editLabel(UUID labelId, LabelRequest labelRequest, UUID userId)
            throws LabelAlreadyExistsException, ForbiddenOperationException {

        String content = labelRequest.getContent();
        Color color = labelRequest.getColor();

        Label findedLabel = labelWorkflowService.findLabelById(labelId);
        UUID boardId = findedLabel.getBoard().getId();

        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);

        // Rol del usuario actual
        Role role = boardAccess.getRole();

        // Si el usuario no tiene el rol de propietario o admin, no podra realizar la
        // operación
        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        if (labelWorkflowServiceImpl.existsLabelByBoardIdAndContentExcludingId(boardId, content, labelId)) {
            throw new LabelAlreadyExistsException();
        }

        findedLabel.setContent(content);
        findedLabel.setColor(color);
        Label savedLabel = labelWorkflowService.saveLabel(findedLabel);

        LabelResponse labelResponse = labelResponseMapper.labelToLabelResponse(savedLabel);
        return labelResponse;
    }

    // Cualquier usuario con el rol de admin o el propietario puede eliminar una
    // etiqueta
    // Se tiene en cuenta que si elimina una etiqueta, se va a quitar de todas las
    // tareas que hayan tenido esa etiqueta eliminada
    @Override
    public void deleteLabel(UUID labelId, UUID userId) throws ForbiddenOperationException {

        Label findedLabel = labelWorkflowService.findLabelById(labelId);
        UUID boardId = findedLabel.getBoard().getId();

        BoardAccess boardAccess = boardAccessWorkflowService.findBoardAccessByBoardIdAndUserIdAndUserActive(boardId,
                userId);
        // Rol del usuario actual
        Role role = boardAccess.getRole();

        // Si el usuario no tiene el rol de propietario o admin, no podra realizar la
        // operación
        if (!BoardAccessRoleUtils.hasAuthorization(role, Role.ADMIN)) {
            throw new ForbiddenOperationException();
        }

        labelWorkflowService.deleteLabelById(labelId);
    }

}
