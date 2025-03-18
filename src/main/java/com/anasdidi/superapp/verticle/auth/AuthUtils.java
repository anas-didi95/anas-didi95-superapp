/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth;

public final class AuthUtils {

  public static final String preparePassword(String plainPassword, String salt) {
    return "%s@#$%s".formatted(plainPassword, salt);
  }
}
