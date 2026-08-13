package com.trello.identity.common;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Standardized API error response based on RFC 7807")
@NoArgsConstructor
@Data
public class StandardizedApiExceptionResponse {
    @Schema(description = "A URI reference that identifies the problem type", name = "type", requiredMode = RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized")
    private String type;

    @Schema(description = "A short, human-readable summary of the problem", name = "title", requiredMode = RequiredMode.REQUIRED, example = "User is not authorized")
    private String title;

    @Schema(description = "A unique error code", name = "code", requiredMode = RequiredMode.NOT_REQUIRED, example = "192")
    private String code;

    @Schema(description = "A human-readable explanation specific to this occurrence of the problem", name = "detail", requiredMode = RequiredMode.REQUIRED, example = "The user does not have the required permissions to access the resource")
    private String detail;

    @Schema(description = "A URI reference that identifies the specific occurrence of the problem", name = "instance", requiredMode = RequiredMode.NOT_REQUIRED, example = "/errors/authentication/not-authorized/01")
    private String instance;

    @Schema(description = "Additional information about fields related to the error", name = "fields", requiredMode = RequiredMode.NOT_REQUIRED)
    private Map<String, String> fields;

    public StandardizedApiExceptionResponse(
            String type,
            String title,
            String code,
            String detail) {

        this.type = type;
        this.title = title;
        this.code = code;
        this.detail = detail;
    }

    public StandardizedApiExceptionResponse(
            String type,
            String title,
            String code,
            String detail,
            Map<String, String> fields) {

        this.type = type;
        this.title = title;
        this.code = code;
        this.detail = detail;
        this.fields = fields;
    }
}