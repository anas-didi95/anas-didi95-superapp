/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.verticle.auth.dto.AuthBaseResDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthLoginReqDto;
import com.anasdidi.superapp.verticle.auth.service.AuthService;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
import java.util.Map;

public class LoginService extends AuthService<AuthLoginReqDto, AuthBaseResDto> {

  public LoginService() {
    super(AuthLoginReqDto.class);
  }

  @Override
  public String getOperationId() {
    return "login";
  }

  @Override
  protected Future<OutboundDto<AuthBaseResDto>> handle(
      InboundDto<AuthLoginReqDto> dto, JsonObject opts) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handle'");
  }

  @Override
  protected AuthLoginReqDto parseMessage(JsonObject body, MultiMap headers) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'parseMessage'");
  }

  @Override
  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'prepareQuery'");
  }

  @Override
  protected JsonObject preparePath(Map<String, RequestParameter> path) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'preparePath'");
  }
}
