package com.kidschedule.api.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFamilyRequest(@NotBlank @Size(max = 50) String name) {
}
