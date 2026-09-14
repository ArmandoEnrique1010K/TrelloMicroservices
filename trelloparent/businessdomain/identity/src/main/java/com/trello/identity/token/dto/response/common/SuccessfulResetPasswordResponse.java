package com.trello.identity.token.dto.response.common;

import com.trello.identity.common.SuccessfulResponse;
import com.trello.identity.user.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulResetPasswordResponse")
public class SuccessfulResetPasswordResponse extends SuccessfulResponse<UserResponse> {

}
