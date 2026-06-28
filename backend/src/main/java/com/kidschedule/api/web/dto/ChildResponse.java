package com.kidschedule.api.web.dto;

import java.util.UUID;

public record ChildResponse(UUID id, String nickname, short birthYear) {
}
