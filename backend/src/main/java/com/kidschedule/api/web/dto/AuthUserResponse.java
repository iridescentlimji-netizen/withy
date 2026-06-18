package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.AccountType;
import java.util.UUID;

public record AuthUserResponse(UUID id, String nickname, AccountType accountType) {
}
