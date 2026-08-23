package com.trello.identity.token.dto.response.common;

import com.trello.identity.common.SuccessfulResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulValidateConfirmAccountTokenResponse")
public class SuccessfulValidateConfirmAccountTokenResponse extends SuccessfulResponse<Void> {

}
