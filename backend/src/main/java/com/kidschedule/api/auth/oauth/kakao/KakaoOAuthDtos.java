package com.kidschedule.api.auth.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

record KakaoTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("expires_in") Long expiresIn) {
}

record KakaoUserResponse(
		Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount, Map<String, Object> properties) {
}

record KakaoAccount(@JsonProperty("profile") KakaoProfile profile, String nickname) {
}

record KakaoProfile(String nickname) {
}
