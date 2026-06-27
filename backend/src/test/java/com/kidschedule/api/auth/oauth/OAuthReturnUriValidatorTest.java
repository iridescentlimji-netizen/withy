package com.kidschedule.api.auth.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kidschedule.api.domain.enums.OAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OAuthReturnUriValidatorTest {

	private OAuthReturnUriValidator validator;

	@BeforeEach
	void setUp() {
		OAuthSecurityProperties properties = new OAuthSecurityProperties();
		properties.setAllowedExpHosts("127.0.0.1,localhost,192.168.45.142");
		validator = new OAuthReturnUriValidator(properties);
	}

	@Test
	void allowsKidScheduleReturnUriForMatchingProvider() {
		String returnUri = validator.resolveReturnUri("kid-schedule://oauth/kakao", OAuthProvider.KAKAO);
		assertEquals("kid-schedule://oauth/kakao", returnUri);
	}

	@Test
	void rejectsKidScheduleReturnUriForMismatchedProvider() {
		assertThrows(
				IllegalArgumentException.class,
				() -> validator.resolveReturnUri("kid-schedule://oauth/google", OAuthProvider.KAKAO));
	}

	@Test
	void allowsExpReturnUriForAllowedHost() {
		String returnUri =
				validator.resolveReturnUri("exp://127.0.0.1:8081/--/oauth/naver", OAuthProvider.NAVER);
		assertEquals("exp://127.0.0.1:8081/--/oauth/naver", returnUri);
	}

	@Test
	void rejectsExpReturnUriForDisallowedHost() {
		assertThrows(
				IllegalArgumentException.class,
				() -> validator.resolveReturnUri("exp://evil.example:8081/--/oauth/kakao", OAuthProvider.KAKAO));
	}

	@Test
	void allowsExactConfiguredReturnUri() {
		OAuthSecurityProperties properties = new OAuthSecurityProperties();
		properties.setAllowedReturnUris("custom://oauth/google");
		OAuthReturnUriValidator customValidator = new OAuthReturnUriValidator(properties);

		String returnUri = customValidator.resolveReturnUri("custom://oauth/google", OAuthProvider.GOOGLE);
		assertEquals("custom://oauth/google", returnUri);
	}

	@Test
	void returnsNullWhenReturnUriMissing() {
		assertNull(validator.resolveReturnUri(null, OAuthProvider.KAKAO));
		assertNull(validator.resolveReturnUri("  ", OAuthProvider.KAKAO));
	}
}
