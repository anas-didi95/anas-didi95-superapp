/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;

public final class AppConfig {

  public static final AppConfig INSTANCE = new AppConfig();
  private JWTOptions jwtOptions;
  private JWTAuth jwtAuth;

  private AppConfig() {}

  void setJwtOptions(JWTOptions jwtOptions) {
    this.jwtOptions = jwtOptions;
  }

  public JWTOptions getJwtOptions() {
    return jwtOptions;
  }

  void setJwtAuth(JWTAuth jwtAuth) {
    this.jwtAuth = jwtAuth;
  }

  public JWTAuth getJwtAuth() {
    return this.jwtAuth;
  }
}
