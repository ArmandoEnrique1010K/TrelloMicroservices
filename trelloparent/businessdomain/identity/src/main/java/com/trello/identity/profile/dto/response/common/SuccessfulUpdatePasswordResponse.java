package com.trello.identity.profile.dto.response.common;

import com.trello.identity.common.SuccessfulResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SuccessfulUpdatePasswordResponse")
public class SuccessfulUpdatePasswordResponse extends SuccessfulResponse<Void> {

}
