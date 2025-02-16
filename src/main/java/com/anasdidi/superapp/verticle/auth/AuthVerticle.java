/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.verticle.auth.service.impl.LoginService;
import java.util.Arrays;
import java.util.List;

public class AuthVerticle extends BaseVerticle {

  @Override
  protected List<BaseService<?, ?>> prepareService() {
    return Arrays.asList(new LoginService());
  }

  @Override
  protected List<String> getLiquibaseLabel() {
    return Arrays.asList("auth");
  }

  @Override
  protected BaseRepository prepareRepository() {
    return new AuthRepository();
  }
}
