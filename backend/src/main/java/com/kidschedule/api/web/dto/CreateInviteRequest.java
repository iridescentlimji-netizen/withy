package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.MemberRole;
import jakarta.validation.constraints.NotNull;

public record CreateInviteRequest(
		@NotNull MemberRole role,
		boolean canEdit) {
}
