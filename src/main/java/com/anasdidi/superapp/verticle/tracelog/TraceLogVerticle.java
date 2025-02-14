/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.verticle.tracelog.service.impl.SaveLogService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TraceLogVerticle extends BaseVerticle {

  @Override
  protected Map<String, BaseService<?, ?>> getServiceMap() {
    return Arrays.asList(new SaveLogService(vertx.eventBus())).stream()
        .collect(Collectors.toMap(o -> o.getOperationId(), Function.identity()));
  }

  @Override
  protected List<String> getLiquibaseLabel() {
    return Arrays.asList("tracelog");
  }
}
