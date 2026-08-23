package com.trello.identity.auth.dto.response.common;

import com.trello.identity.auth.dto.response.AccountResponse;
import com.trello.identity.common.SuccessfulResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulAccountResponse")
public class SuccessfulAccountResponse extends SuccessfulResponse<AccountResponse> {
}
