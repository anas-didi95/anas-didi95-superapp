/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common.enums;

public enum ModuleEnum {
  M000_HELLO_WORLD("M000"),
  M001_TRACE_LOG("M001"),
  MOO2_AUTH("M002");

  public final String code;

  ModuleEnum(String code) {
    this.code = code;
  }
}
