package com.trello.workflow.label.service;

import java.util.List;

import com.trello.workflow.label.dto.response.ColorResponse;

public interface ColorService {
    List<ColorResponse> listColors();
}
