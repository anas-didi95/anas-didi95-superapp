/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld.dto;

import com.anasdidi.superapp.common.BaseReqDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface HelloWorldBaseReqDto extends BaseReqDto {}
