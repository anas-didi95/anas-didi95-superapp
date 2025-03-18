/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.dto.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthUser(
    UUID userId, String username, Instant validAfterDate, List<String> permissions) {}
