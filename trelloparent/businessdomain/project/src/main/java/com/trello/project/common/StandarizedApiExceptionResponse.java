package com.trello.project.common;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Respuesta de error de API estandarizada basada en RFC 9457")
@NoArgsConstructor
@Data
public class StandarizedApiExceptionResponse {
    @Schema(description = "Una referencia URI que identifica el tipo de problema", name = "type", example = "/errors/authentication/not-authorized")
    private String type;

    @Schema(description = "Un resumen breve y comprensible para las personas sobre el problema", name = "title", example = "User is not authorized")
    private String title;

    @Schema(description = "Un código de estado único", name = "status", example = "500")
    private int status;

    @Schema(description = "Una explicación legible por humanos específica para esta aparición del problema", name = "detail", example = "The user does not have the required permissions to access the resource")
    private String detail;

    @Schema(description = "Una referencia URI que identifica la aparición específica del problema", name = "instance", example = "/errors/authentication/not-authorized/01")
    private String instance;

    @Schema(description = "Un mensaje legible para mostrar en el cliente web", name = "message", example = "Vuelva ha iniciar sesión en la aplicación")
    private String message;

    @Schema(description = "Información adicional sobre los campos relacionados con el error", name = "fields", example = "{\"campo1\":\"mensaje\",\"campo2\":\"mensaje\"}")
    private Map<String, String> fields;

    public StandarizedApiExceptionResponse(
            String type,
            String title,
            int status,
            String detail,
            String instance,
            String message) {

        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.message = message;
    }

    public StandarizedApiExceptionResponse(
            String type,
            String title,
            int status,
            String detail,
            String instance,
            String message,
            Map<String, String> fields) {

        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.message = message;
        this.fields = fields;
    }

}
