package com.trello.identity.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AccountResponse", description = "Represents a user on database")
public class AccountResponse {
    @Schema(name = "firstName", example = "John", description = "FirstName of the user on database")
    private String firstName;
    @Schema(name = "lastName", example = "Doe", description = "LastName of the user on database")
    private String lastName;
    @Schema(name = "email", example = "example@gmail.com", description = "Email of the user on database")
    private String email;
}
