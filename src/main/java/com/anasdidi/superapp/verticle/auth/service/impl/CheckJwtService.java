/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.verticle.auth.dto.AuthCheckJwtReqDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthCheckJwtResDto;
import com.anasdidi.superapp.verticle.auth.service.AuthService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.openapi.validation.RequestParameter;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class CheckJwtService extends AuthService<AuthCheckJwtReqDto, AuthCheckJwtResDto> {

  public CheckJwtService() {
    super(AuthCheckJwtReqDto.class);
  }

  @Override
  public String getOperationId() {
    return "checkJwt";
  }

  @Override
  protected Future<OutboundDto<AuthCheckJwtResDto>> handle(
      User user, InboundDto<AuthCheckJwtReqDto> dto, Map<String, Object> opts) {
    String userId = "userId" + System.currentTimeMillis();
    OffsetDateTime issuedAt = OffsetDateTime.now().minus(30, ChronoUnit.MINUTES);
    OffsetDateTime expiredAt = OffsetDateTime.now();
    return Future.succeededFuture(
        new OutboundDto<>(new AuthCheckJwtResDto(userId, issuedAt, expiredAt), false));
  }

  @Override
  protected AuthCheckJwtReqDto parseMessage(JsonObject body, MultiMap headers) {
    return new AuthCheckJwtReqDto();
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
