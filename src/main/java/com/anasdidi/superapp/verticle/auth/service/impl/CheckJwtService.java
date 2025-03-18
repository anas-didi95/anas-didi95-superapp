/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.error.E03RecordNotFoundError;
import com.anasdidi.superapp.error.E03RecordNotFoundError.E003RecordEnum;
import com.anasdidi.superapp.error.E05UserSessionExpiredError;
import com.anasdidi.superapp.verticle.auth.AuthRepository;
import com.anasdidi.superapp.verticle.auth.dto.AuthCheckJwtReqDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthCheckJwtResDto;
import com.anasdidi.superapp.verticle.auth.dto.model.AuthUser;
import com.anasdidi.superapp.verticle.auth.entity.UserSessionEntity;
import com.anasdidi.superapp.verticle.auth.service.AuthService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.sqlclient.SqlConnection;
import java.util.Map;
import java.util.Objects;

public class CheckJwtService extends AuthService<AuthCheckJwtReqDto, AuthCheckJwtResDto> {

  public CheckJwtService() {
    super(AuthCheckJwtReqDto.class);
  }

  @Override
  public String getOperationId() {
    return "checkJwt";
  }

  @Override
  protected String getPermission() {
    return "P003";
  }

  @Override
  protected Future<OutboundDto<AuthCheckJwtResDto>> handle(
      User user, InboundDto<AuthCheckJwtReqDto> dto, Map<String, Object> opts, String traceId) {
    AuthUser authUser = user.principal().mapTo(AuthUser.class);
    AuthRepository repo = getRepository();
    Future<SqlConnection> conn = repo.getConnection();
    Future<UserSessionEntity> session =
        conn.compose(o -> repo.getUserSessionByUserId(o, authUser.userId()))
            .andThen(
                o -> {
                  UserSessionEntity entity = o.result();
                  if (Objects.isNull(entity)) {
                    throw new E03RecordNotFoundError(E003RecordEnum.UserSessionEntity);
                  } else if (authUser
                      .validAfterDate()
                      .isBefore(entity.getValidAfterDate().toInstant())) {
                    throw new E05UserSessionExpiredError();
                  }
                });

    return Future.future(
        promise -> {
          Future.all(conn, session)
              .onComplete(
                  o ->
                      promise.complete(
                          new OutboundDto<>(
                              new AuthCheckJwtResDto(
                                  authUser.username(), session.result().getValidAfterDate()))),
                  e -> promise.fail(e));
        });
  }

  @Override
  protected AuthCheckJwtReqDto parseMessage(JsonObject body, MultiMap headers) {
    return new AuthCheckJwtReqDto();
  }

  @Override
  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return null;
  }

  @Override
  protected JsonObject preparePath(Map<String, RequestParameter> path) {
    return null;
  }
}
