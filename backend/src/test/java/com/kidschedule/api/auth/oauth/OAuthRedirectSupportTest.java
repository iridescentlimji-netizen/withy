package com.kidschedule.api.auth.oauth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kidschedule.api.domain.enums.OAuthProvider;
import org.junit.jupiter.api.Test;

class OAuthRedirectSupportTest {

	@Test
	void assertProvider_acceptsMatchingProvider() {
		OAuthState oauthState = new OAuthState(OAuthProvider.KAKAO, "http://localhost/redirect", "kid-schedule://oauth/kakao");

		assertDoesNotThrow(() -> OAuthRedirectSupport.assertProvider(oauthState, OAuthProvider.KAKAO));
	}

	@Test
	void assertProvider_rejectsMismatchedProvider() {
		OAuthState oauthState = new OAuthState(OAuthProvider.GOOGLE, "http://localhost/redirect", "kid-schedule://oauth/google");

		assertThrows(
				IllegalArgumentException.class,
				() -> OAuthRedirectSupport.assertProvider(oauthState, OAuthProvider.KAKAO));
	}
}
