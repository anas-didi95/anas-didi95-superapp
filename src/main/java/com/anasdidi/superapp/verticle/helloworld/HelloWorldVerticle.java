/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.verticle.helloworld.service.impl.GreetService;
import java.util.Arrays;
import java.util.List;

public class HelloWorldVerticle extends BaseVerticle {

  @Override
  protected List<BaseService<?, ?>> prepareService() {
    return Arrays.asList(new GreetService());
  }

  @Override
  protected List<String> getLiquibaseLabel() {
    return Arrays.asList("helloworld");
  }

  @Override
  protected BaseRepository getRepository() {
    throw new UnsupportedOperationException("Unimplemented method 'getRepository'");
  }
}
