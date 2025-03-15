/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserSessionEntity {

  private UUID id;
  private UUID userId;
  private OffsetDateTime validAfterDate;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public OffsetDateTime getValidAfterDate() {
    return validAfterDate;
  }

  public void setValidAfterDate(OffsetDateTime validAfterDate) {
    this.validAfterDate = validAfterDate;
  }
}
