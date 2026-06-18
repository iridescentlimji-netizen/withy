package com.kidschedule.api.auth.oauth.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

record NaverTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("expires_in") Long expiresIn) {
}

record NaverUserEnvelope(NaverUserResponse response) {
}

record NaverUserResponse(String id, String nickname, String name) {
}
