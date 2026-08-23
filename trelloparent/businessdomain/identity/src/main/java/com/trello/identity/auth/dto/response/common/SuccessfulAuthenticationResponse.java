package com.trello.identity.auth.dto.response.common;

import com.trello.identity.auth.dto.response.AuthenticationResponse;
import com.trello.identity.common.SuccessfulResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulAuthenticationResponse")
public class SuccessfulAuthenticationResponse extends SuccessfulResponse<AuthenticationResponse> {
}
