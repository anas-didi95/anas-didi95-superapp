/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

public final class CommonUtils {

  private static final String EVENTBUS_ADDR_FMT = "%s:%s";

  public static final String prepareEventBusAddress(Class<?> clazz, String eventType) {
    return prepareEventBusAddress(clazz.getSimpleName(), eventType);
  }

  public static final String prepareEventBusAddress(String prefix, String eventType) {
    return EVENTBUS_ADDR_FMT.formatted(prefix, eventType);
  }
}
