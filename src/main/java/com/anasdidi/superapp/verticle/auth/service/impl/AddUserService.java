/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service.impl;

import com.anasdidi.superapp.AppConfig;
import com.anasdidi.superapp.verticle.auth.AuthRepository;
import com.anasdidi.superapp.verticle.auth.AuthUtils;
import com.anasdidi.superapp.verticle.auth.dto.AuthAddUserReqDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthAddUserResDto;
import com.anasdidi.superapp.verticle.auth.service.AuthService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.prng.VertxContextPRNG;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import java.util.Map;

public class AddUserService extends AuthService<AuthAddUserReqDto, AuthAddUserResDto> {

  public AddUserService() {
    this(AuthAddUserReqDto.class);
  }

  public AddUserService(Class<AuthAddUserReqDto> bodyClass) {
    super(bodyClass);
  }

  @Override
  public String getOperationId() {
    return "addUser";
  }

  @Override
  protected String getPermission() {
    return "P002";
  }

  @Override
  protected Future<OutboundDto<AuthAddUserResDto>> handle(
      User user, InboundDto<AuthAddUserReqDto> dto, Map<String, Object> opts, String traceId) {
    AuthRepository repo = getRepository();
    String salt = VertxContextPRNG.current(vertx).nextString(32);

    Future<String> hashPassword =
        Future.future(
            promise -> {
              String fullPassword = AuthUtils.preparePassword(dto.body().password(), salt);
              promise.complete(AppConfig.INSTANCE.getPasswordEncoder().encode(fullPassword));
            });
    Future<SqlConnection> conn = repo.getConnection();
    Future<Transaction> tran = conn.compose(o -> o.begin());
    Future<Void> insert =
        Future.all(hashPassword, conn, tran)
            .compose(
                o ->
                    repo.insertUser(
                        conn.result(), dto.body().username(), hashPassword.result(), salt));

    return Future.future(
        promise -> {
          Future.all(hashPassword, conn, tran, insert)
              .onComplete(
                  o -> {
                    tran.compose(oo -> oo.commit()).eventually(() -> conn.result().close());
                    promise.complete(new OutboundDto<>(new AuthAddUserResDto(traceId)));
                  },
                  e -> {
                    tran.compose(oo -> oo.rollback()).eventually(() -> conn.result().close());
                  });
        });
  }

  @Override
  protected AuthAddUserReqDto parseMessage(JsonObject body, MultiMap headers) {
    throw new UnsupportedOperationException("Unimplemented method 'parseMessage'");
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
