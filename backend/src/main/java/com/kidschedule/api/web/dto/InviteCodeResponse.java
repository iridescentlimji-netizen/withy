package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.MemberRole;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record InviteCodeResponse(String code, MemberRole role, boolean canEdit, Instant expiresAt) {
}
