/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.AppConfig;
import com.anasdidi.superapp.error.E04InvalidUsernamePasswordError;
import com.anasdidi.superapp.verticle.auth.AuthRepository;
import com.anasdidi.superapp.verticle.auth.dto.AuthLoginReqDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthLoginResDto;
import com.anasdidi.superapp.verticle.auth.dto.model.AuthUser;
import com.anasdidi.superapp.verticle.auth.entity.UserEntity;
import com.anasdidi.superapp.verticle.auth.service.AuthService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.sqlclient.SqlConnection;
import java.util.Arrays;
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
      User user, InboundDto<AuthLoginReqDto> dto, Map<String, Object> opts) {
    AuthRepository repo = getRepository();
    Future<SqlConnection> conn = repo.getConnection();
    Future<UserEntity> entity =
        conn.compose(o -> repo.getUserByUsername(o, dto.body().username()))
            .andThen(
                o -> {
                  if (o.failed() || !o.result().getPassword().equals(dto.body().password())) {
                    throw new E04InvalidUsernamePasswordError();
                  }
                });

    return Future.future(
        promise -> {
          Future.all(conn, entity)
              .onComplete(
                  o -> {
                    JWTAuth jwt = AppConfig.INSTANCE.getJwtAuth();
                    AuthUser userData = new AuthUser(entity.result().getUsername());
                    String accessToken =
                        jwt.generateToken(
                            JsonObject.mapFrom(userData),
                            new JWTOptions(AppConfig.INSTANCE.getJwtOptions())
                                .setSubject(entity.result().getId().toString())
                                .setAudience(Arrays.asList("DEV")));
                    promise.complete(new OutboundDto<>(new AuthLoginResDto(accessToken), false));
                  },
                  e -> promise.fail(e))
              .eventually(() -> conn.compose(o -> o.close()));
        });
  }

  @Override
  protected AuthLoginReqDto parseMessage(JsonObject body, MultiMap headers) {
    return new AuthLoginReqDto(null, null);
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
