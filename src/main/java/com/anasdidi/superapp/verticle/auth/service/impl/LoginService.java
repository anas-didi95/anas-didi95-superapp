/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.verticle.auth.dto.AuthLoginReqDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthLoginResDto;
import com.anasdidi.superapp.verticle.auth.dto.model.AuthUser;
import com.anasdidi.superapp.verticle.auth.service.AuthService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.openapi.validation.RequestParameter;
import java.util.Map;

public class LoginService extends AuthService<AuthLoginReqDto, AuthLoginResDto> {

  public LoginService() {
    super(AuthLoginReqDto.class);
  }

  @Override
  public String getOperationId() {
    return "login";
  }

  @Override
  protected Future<OutboundDto<AuthLoginResDto>> handle(
      InboundDto<AuthLoginReqDto> dto, Map<String, Object> opts) {
    JWTAuth jwt =
        JWTAuth.create(
            getVertx(),
            new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions().setAlgorithm("HS256").setBuffer("secret")));
    JWTAuthHandler.create(jwt);
    AuthUser user = new AuthUser("USER_ID_123");
    return Future.succeededFuture(
        new OutboundDto<>(
            new AuthLoginResDto(
                jwt.generateToken(
                    JsonObject.mapFrom(user),
                    new JWTOptions().setSubject(user.userId()).setIssuer("super-app"))),
            false));
  }

  @Override
  protected AuthLoginReqDto parseMessage(JsonObject body, MultiMap headers) {
    return new AuthLoginReqDto(null, null);
  }

  @Override
  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return JsonObject.of();
  }

  @Override
  protected JsonObject preparePath(Map<String, RequestParameter> path) {
    return JsonObject.of();
  }
}
