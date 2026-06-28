package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.MemberRole;
import java.util.UUID;

public record FamilyResponse(UUID id, String name, MemberRole role, boolean canEdit) {
}
