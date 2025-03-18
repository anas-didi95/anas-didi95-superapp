/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.dto;

import java.time.OffsetDateTime;

public record AuthCheckJwtResDto(String username, OffsetDateTime issuedAt)
    implements AuthBaseResDto {}
