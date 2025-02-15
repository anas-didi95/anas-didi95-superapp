/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.service.impl;

import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogReqDto;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogResDto;
import com.anasdidi.superapp.verticle.tracelog.service.TraceLogService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

public class SaveLogService extends TraceLogService<TraceLogSaveLogReqDto, TraceLogSaveLogResDto> {

  public SaveLogService() {
    super(TraceLogSaveLogReqDto.class);
  }

  @Override
  public String getOperationId() {
    return "saveLog";
  }

  @Override
  protected TraceLogSaveLogResDto handle(InboundDto<TraceLogSaveLogReqDto> dto, JsonObject opts) {
    Future<SqlConnection> conn = this.repository.getConnection();
    Future<Transaction> tran = conn.compose(o -> o.begin());

    String sql =
        "INSERT INTO TBL_TRACE_LOG (CREATE_BY, CREATE_DT, TRACE_ID, ORIGIN, IN_PAYLOAD, OPTS, OUT_PAYLOAD, IS_ERR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    Future<?> insert =
        conn.compose(
            o ->
                o.preparedQuery(sql)
                    .execute(
                        Tuple.of(
                            "SYSTEM",
                            OffsetDateTime.now(),
                            dto.body().traceId(),
                            dto.body().origin(),
                            dto.body().in().encode(),
                            Optional.ofNullable(dto.body().opts()).orElse(JsonObject.of()).encode(),
                            dto.body().out().encode(),
                            false)));

    Future.all(conn, tran, insert)
        .onComplete(
            o -> {
              tran.result().commit();
              conn.result().close();
            },
            e -> {
              System.out.println(e);
              e.printStackTrace();
              tran.result().rollback();
              conn.result().close();
            });
    return new TraceLogSaveLogResDto();
  }

  @Override
  protected TraceLogSaveLogReqDto parseMessage(JsonObject body, MultiMap headers) {
    String traceId = headers.get("EV_TRACEID");
    String origin = headers.get("EV_ORIGIN");
    JsonObject in = body.getJsonObject("in");
    JsonObject out = body.getJsonObject("out");
    JsonObject opts = body.getJsonObject("opts");
    return new TraceLogSaveLogReqDto(traceId, origin, in, out, opts);
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
