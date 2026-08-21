package com.trello.identity.common;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Respuesta de error de API estandarizada basada en RFC 9457")
@NoArgsConstructor
@Data
public class StandarizedApiExceptionResponse {
    @Schema(description = "Una referencia URI que identifica el tipo de problema", name = "type", requiredMode = RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized")
    private String type;

    @Schema(description = "Un resumen breve y comprensible para las personas sobre el problema", name = "title", requiredMode = RequiredMode.REQUIRED, example = "User is not authorized")
    private String title;

    @Schema(description = "Un código de estado único", name = "status", requiredMode = RequiredMode.REQUIRED, example = "500")
    private int status;

    @Schema(description = "Una explicación legible por humanos específica para esta aparición del problema", name = "detail", requiredMode = RequiredMode.REQUIRED, example = "The user does not have the required permissions to access the resource")
    private String detail;

    @Schema(description = "Una referencia URI que identifica la aparición específica del problema", name = "instance", requiredMode = RequiredMode.NOT_REQUIRED, example = "/errors/authentication/not-authorized/01")
    private String instance;

    // Campos personalizados
    @Schema(description = "Un mensaje legible para mostrar en el cliente web", name = "message", requiredMode = RequiredMode.REQUIRED, example = "Message to show in web client")
    private String message;

    @Schema(description = "Información adicional sobre los campos relacionados con el error", name = "fields", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\"additionalProp1\":\"string\",\"additionalProp2\":\"string\"}")
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