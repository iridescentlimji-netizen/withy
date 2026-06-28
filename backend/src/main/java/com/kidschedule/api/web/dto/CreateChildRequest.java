package com.kidschedule.api.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChildRequest(
		@NotBlank @Size(max = 50) String nickname,
		@NotNull @Min(2000) @Max(2100) Short birthYear) {
}
