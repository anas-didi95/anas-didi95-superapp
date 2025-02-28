/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.service.impl;

import com.anasdidi.superapp.verticle.tracelog.TraceLogRepository;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogReqDto;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogResDto;
import com.anasdidi.superapp.verticle.tracelog.service.TraceLogService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import java.util.Map;

public class SaveLogService extends TraceLogService<TraceLogSaveLogReqDto, TraceLogSaveLogResDto> {

  public SaveLogService() {
    super(TraceLogSaveLogReqDto.class);
  }

  @Override
  public String getOperationId() {
    return "saveLog";
  }

  @Override
  protected Future<OutboundDto<TraceLogSaveLogResDto>> handle(
      InboundDto<TraceLogSaveLogReqDto> dto, Map<String, Object> opts) {
    return Future.future(
        promise -> {
          Future<SqlConnection> conn = this.getRepository(TraceLogRepository.class).getConnection();
          Future<Transaction> tran = conn.compose(o -> o.begin());
          Future<Void> insert =
              Future.all(conn, tran)
                  .compose(
                      o ->
                          this.getRepository(TraceLogRepository.class)
                              .saveLog(conn.result(), dto.body()));

          Future.all(conn, tran, insert)
              .onComplete(
                  o -> {
                    tran.result().commit().eventually(() -> conn.result().close());
                    promise.complete(new OutboundDto<>(new TraceLogSaveLogResDto(), false));
                  },
                  e -> {
                    tran.result().rollback().eventually(() -> conn.result().close());
                    promise.fail(e);
                  });
        });
  }

  @Override
  protected TraceLogSaveLogReqDto parseMessage(JsonObject body, MultiMap headers) {
    String traceId = headers.get("EV_TRACEID");
    String origin = headers.get("EV_ORIGIN");
    JsonObject in = body.getJsonObject("in");
    JsonObject out = body.getJsonObject("out");
    JsonObject opts = body.getJsonObject("opts");
    Boolean isError = body.getBoolean("isError");
    return new TraceLogSaveLogReqDto(traceId, origin, in, out, opts, isError);
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
