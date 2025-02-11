/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.ext.web.RoutingContext;

public interface BaseService {

  void process(RoutingContext ctx);

  String getOperationId();
}
