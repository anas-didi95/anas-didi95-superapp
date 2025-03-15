/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.dto;

public record AuthAddUserReqDto(String username, String password) implements AuthBaseReqDto {}
