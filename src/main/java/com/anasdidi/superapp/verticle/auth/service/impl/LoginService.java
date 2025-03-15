/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.AppConfig;
import com.anasdidi.superapp.error.E04InvalidUsernamePasswordError;
import com.anasdidi.superapp.verticle.auth.AuthRepository;
import com.anasdidi.superapp.verticle.auth.AuthUtils;
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
import io.vertx.sqlclient.Transaction;
import java.time.Instant;
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
      User user, InboundDto<AuthLoginReqDto> dto, Map<String, Object> opts, String traceId) {
    AuthRepository repo = getRepository();
    Future<SqlConnection> conn = repo.getConnection();
    Future<Transaction> tran = conn.compose(o -> o.begin());
    Future<UserEntity> entity =
        conn.compose(o -> repo.getUserByUsername(o, dto.body().username()))
            .andThen(
                o -> {
                  if (o.failed()) {
                    throw new E04InvalidUsernamePasswordError();
                  } else if (!AppConfig.INSTANCE
                      .getPasswordEncoder()
                      .matches(
                          AuthUtils.preparePassword(dto.body().password(), o.result().getSalt()),
                          o.result().getPassword())) {
                    throw new E04InvalidUsernamePasswordError();
                  }
                });
    Future<Void> session =
        Future.all(conn, tran, entity)
            .compose(o -> repo.upsertUserSession(conn.result(), entity.result().getId()));

    return Future.future(
        promise -> {
          Future.all(conn, tran, entity, session)
              .onComplete(
                  o -> {
                    JWTAuth jwt = AppConfig.INSTANCE.getJwtAuth();
                    AuthUser userData =
                        new AuthUser(
                            entity.result().getId(), entity.result().getUsername(), Instant.now());
                    String accessToken =
                        jwt.generateToken(
                            JsonObject.mapFrom(userData),
                            new JWTOptions(AppConfig.INSTANCE.getJwtOptions())
                                .setSubject(entity.result().getUsername())
                                .setAudience(Arrays.asList("DEV")));
                    tran.compose(oo -> oo.commit()).eventually(() -> conn.result().close());
                    promise.complete(new OutboundDto<>(new AuthLoginResDto(accessToken)));
                  },
                  e -> {
                    tran.compose(oo -> oo.rollback()).eventually(() -> conn.result().close());
                    promise.fail(e);
                  });
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
