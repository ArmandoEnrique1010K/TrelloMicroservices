package com.trello.identity.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "RefreshTokenRequest", description = "Token de revalidación utilizado para obtener un nuevo accessToken")
public class RefreshTokenRequest {

    @Schema(name = "refreshToken", requiredMode = RequiredMode.REQUIRED, example = "eyJ...", description = "Token de revalidación")
    @NotBlank
    private String refreshToken;
}