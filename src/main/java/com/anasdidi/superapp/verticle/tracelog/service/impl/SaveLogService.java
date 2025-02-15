/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.service.impl;

import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogReqDto;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogResDto;
import com.anasdidi.superapp.verticle.tracelog.service.TraceLogService;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
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
  protected TraceLogSaveLogResDto handle(InboundDto<TraceLogSaveLogReqDto> dto, JsonObject opts) {
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
