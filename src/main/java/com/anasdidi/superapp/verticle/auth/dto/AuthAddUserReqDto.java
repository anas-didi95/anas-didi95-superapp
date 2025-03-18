/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.dto;

import java.util.UUID;

public record AuthAddUserReqDto(String username, String password, String channelId, UUID roleId)
    implements AuthBaseReqDto {}
