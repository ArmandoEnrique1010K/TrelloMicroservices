package com.trello.workflow.label.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trello.workflow.enums.Color;
import com.trello.workflow.label.dto.response.ColorResponse;
import com.trello.workflow.label.mapper.ColorResponseMapper;

@Service
public class ColorServiceImpl implements ColorService {

    private final ColorResponseMapper colorResponseMapper;

    public ColorServiceImpl(ColorResponseMapper colorResponseMapper) {
        this.colorResponseMapper = colorResponseMapper;
    }

    @Override
    public List<ColorResponse> listColors() {
        return colorResponseMapper.colorListToColorResponseList(List.of(Color.values()));
    }
}
