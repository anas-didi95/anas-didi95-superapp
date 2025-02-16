/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.service;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.verticle.auth.dto.AuthBaseReqDto;
import com.anasdidi.superapp.verticle.auth.dto.AuthBaseResDto;

public abstract class AuthService<A extends AuthBaseReqDto, B extends AuthBaseResDto>
    extends BaseService<A, B> {

  public AuthService(Class<A> bodyClass) {
    super(bodyClass);
  }
}
