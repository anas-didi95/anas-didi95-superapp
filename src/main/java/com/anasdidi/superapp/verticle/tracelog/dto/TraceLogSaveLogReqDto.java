/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.dto;

import io.vertx.core.json.JsonObject;

public record TraceLogSaveLogReqDto(
    String traceId, String origin, JsonObject in, JsonObject out, JsonObject opts, Boolean isError)
    implements TraceLogBaseReqDto {}
