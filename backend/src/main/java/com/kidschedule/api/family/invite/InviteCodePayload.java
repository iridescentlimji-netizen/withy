package com.kidschedule.api.family.invite;

import com.kidschedule.api.domain.enums.MemberRole;
import java.util.UUID;

public record InviteCodePayload(
		UUID familyId, MemberRole role, boolean canEdit, UUID createdByUserId) {
}
