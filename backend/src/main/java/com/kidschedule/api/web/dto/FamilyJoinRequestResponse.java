package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.JoinStatus;
import com.kidschedule.api.domain.enums.MemberRole;
import java.util.UUID;

public record FamilyJoinRequestResponse(
		UUID id,
		UUID familyId,
		String familyName,
		UUID userId,
		String userNickname,
		MemberRole role,
		boolean canEdit,
		JoinStatus status) {
}
