/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog.service;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogBaseReqDto;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogBaseResDto;

public abstract class TraceLogService<A extends TraceLogBaseReqDto, B extends TraceLogBaseResDto>
    extends BaseService<A, B> {

  public TraceLogService(Class<A> bodyClass) {
    super(bodyClass);
  }
}
