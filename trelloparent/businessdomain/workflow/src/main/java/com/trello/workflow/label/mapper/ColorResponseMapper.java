package com.trello.workflow.label.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.trello.workflow.enums.Color;
import com.trello.workflow.label.dto.response.ColorResponse;

@Mapper(componentModel = "spring")
public interface ColorResponseMapper {
    // Recordar que Color es un enum, no una entidad
    @Mappings({
            @Mapping(target = "id", source = "color")
    })
    ColorResponse colorToColorResponse(Color color);

    List<ColorResponse> colorListToColorResponseList(List<Color> colors);
}
