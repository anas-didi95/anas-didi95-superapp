/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.verticle.tracelog.service.impl.SaveLogService;
import java.util.Arrays;
import java.util.List;

public class TraceLogVerticle extends BaseVerticle {

  @Override
  protected List<BaseService<?, ?, ?>> prepareService() {
    return Arrays.asList(new SaveLogService());
  }

  @Override
  protected List<String> getLiquibaseLabel() {
    return Arrays.asList("tracelog");
  }

  @Override
  protected BaseRepository prepareRepository() {
    return new TraceLogRepository();
  }
}
