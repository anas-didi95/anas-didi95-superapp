/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld.service;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldBaseReqDto;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldBaseResDto;
import io.vertx.core.eventbus.EventBus;

public abstract class HelloWorldService<
        A extends HelloWorldBaseReqDto, B extends HelloWorldBaseResDto>
    extends BaseService<A, B> {

  public HelloWorldService(Class<A> bodyClass, EventBus eventBus) {
    super(bodyClass, eventBus);
  }
}
