package com.kidschedule.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinFamilyRequest(@NotBlank @Size(min = 8, max = 8) String code) {
}
