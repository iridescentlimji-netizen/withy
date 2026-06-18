package com.kidschedule.api.auth;

import com.kidschedule.api.domain.enums.AccountType;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, AccountType accountType, String nickname) {
}
