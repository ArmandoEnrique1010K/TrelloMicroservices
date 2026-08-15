package com.trello.identity.auth.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AccountResponse", description = "Represents a user on database")
public class AccountResponse {
    @Schema(name = "id", example = "John", description = "ID of the user on database")
    private UUID id;
    @Schema(name = "email", example = "example@gmail.com", description = "Email of the user on database")
    private String email;
    @Schema(name = "confirmed", example = "false", description = "Is the user active in the database?")
    private boolean confirmed;
}
