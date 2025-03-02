/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import io.vertx.ext.auth.jwt.JWTAuth;

public final class AppConfig {

  public static final AppConfig INSTANCE = new AppConfig();
  private JWTAuth jwtAuth;

  private AppConfig() {}

  void setJwtAuth(JWTAuth jwtAuth) {
    this.jwtAuth = jwtAuth;
  }

  public JWTAuth getJwtAuth() {
    return this.jwtAuth;
  }
}
