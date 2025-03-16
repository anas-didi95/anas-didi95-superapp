/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.service;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.common.enums.ModuleEnum;
import com.anasdidi.superapp.verticle.tracelog.TraceLogRepository;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogBaseReqDto;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogBaseResDto;

public abstract class TraceLogService<A extends TraceLogBaseReqDto, B extends TraceLogBaseResDto>
    extends BaseService<A, B, TraceLogRepository> {

  public TraceLogService(Class<A> bodyClass) {
    super(bodyClass, TraceLogRepository.class, ModuleEnum.M001_TRACE_LOG);
  }
}
