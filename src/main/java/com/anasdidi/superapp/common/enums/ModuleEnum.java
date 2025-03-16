/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common.enums;

public enum ModuleEnum {
  M001_DEV("M001"),
  M002_TRACE_LOG("M002"),
  MOO3_AUTH("M003");

  public final String code;

  ModuleEnum(String code) {
    this.code = code;
  }
}
