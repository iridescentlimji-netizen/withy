package com.kidschedule.api.web.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoCallbackRequest(@NotBlank String code, @NotBlank String state) {
}
