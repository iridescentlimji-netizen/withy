package com.kidschedule.api.auth.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;

record GoogleTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("expires_in") Long expiresIn) {
}

record GoogleUserResponse(String sub, String name, String email, @JsonProperty("picture") String picture) {
}
