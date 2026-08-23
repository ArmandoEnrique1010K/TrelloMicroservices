package com.trello.identity.token.dto.response.common;

import com.trello.identity.common.SuccessfulResponse;
import com.trello.identity.token.dto.response.ValidatePasswordResetTokenResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulValidatePasswordResetTokenResponse")
public class SuccessfulValidatePasswordResetTokenResponse
        extends SuccessfulResponse<ValidatePasswordResetTokenResponse> {

}
