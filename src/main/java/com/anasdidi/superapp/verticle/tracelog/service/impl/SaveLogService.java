/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.service.impl;

import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogReqDto;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogResDto;
import com.anasdidi.superapp.verticle.tracelog.service.TraceLogService;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class SaveLogService extends TraceLogService<TraceLogSaveLogReqDto, TraceLogSaveLogResDto> {

  public SaveLogService(EventBus eventBus) {
    super(TraceLogSaveLogReqDto.class, eventBus);
  }

  public SaveLogService(Class<TraceLogSaveLogReqDto> bodyClass, EventBus eventBus) {
    super(bodyClass, eventBus);
  }

  @Override
  public String getOperationId() {
    return "saveLog";
  }

  @Override
  protected TraceLogSaveLogResDto handle(InboundDto<TraceLogSaveLogReqDto> dto) {
    return new TraceLogSaveLogResDto();
  }

  @Override
  protected TraceLogSaveLogReqDto parseMessage(JsonObject body, MultiMap headers) {
    String traceId = headers.get("EV_TRACEID");
    String origin = headers.get("EV_ORIGIN");
    JsonObject in = body.getJsonObject("in");
    JsonObject out = body.getJsonObject("out");
    return new TraceLogSaveLogReqDto(traceId, origin, in, out);
  }
}
