package com.kidschedule.api.web.dto;

public record AuthResponse(
		String accessToken, String tokenType, long expiresIn, AuthUserResponse user) {
}
