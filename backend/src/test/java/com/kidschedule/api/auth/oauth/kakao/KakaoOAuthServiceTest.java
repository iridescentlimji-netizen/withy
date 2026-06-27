package com.kidschedule.api.auth.oauth.kakao;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kidschedule.api.auth.oauth.OAuthReturnUriValidator;
import com.kidschedule.api.auth.oauth.OAuthState;
import com.kidschedule.api.auth.oauth.OAuthStateStore;
import com.kidschedule.api.auth.oauth.OAuthUserProvisioner;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.web.dto.OAuthCallbackRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoOAuthServiceTest {

	@Mock
	private KakaoOAuthProperties properties;

	@Mock
	private KakaoOAuthClient kakaoOAuthClient;

	@Mock
	private OAuthStateStore oAuthStateStore;

	@Mock
	private OAuthUserProvisioner oAuthUserProvisioner;

	@Mock
	private OAuthReturnUriValidator oAuthReturnUriValidator;

	@InjectMocks
	private KakaoOAuthService kakaoOAuthService;

	@Test
	void loginWithAuthorizationCode_rejectsProviderMismatchBeforeExternalCalls() {
		when(properties.getRestApiKey()).thenReturn("test-kakao-key");
		when(oAuthStateStore.consumeState("state-1"))
				.thenReturn(new OAuthState(
						OAuthProvider.GOOGLE,
						"http://localhost:8080/api/v1/auth/kakao/redirect",
						"kid-schedule://oauth/kakao"));

		assertThrows(
				IllegalArgumentException.class,
				() -> kakaoOAuthService.loginWithAuthorizationCode(new OAuthCallbackRequest("code-1", "state-1")));

		verify(kakaoOAuthClient, never()).exchangeCodeForToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
		verify(oAuthUserProvisioner, never()).issueAuthResponse(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
	}
}
