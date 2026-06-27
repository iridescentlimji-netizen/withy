package com.kidschedule.api.auth.oauth.kakao;

import com.kidschedule.api.auth.oauth.OAuthRedirectSupport;
import com.kidschedule.api.auth.oauth.OAuthReturnUriValidator;
import com.kidschedule.api.auth.oauth.OAuthStateStore;
import com.kidschedule.api.auth.oauth.OAuthUserProvisioner;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.OAuthCallbackRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KakaoOAuthService {

	private final KakaoOAuthProperties properties;
	private final KakaoOAuthClient kakaoOAuthClient;
	private final OAuthStateStore oAuthStateStore;
	private final OAuthUserProvisioner oAuthUserProvisioner;
	private final OAuthReturnUriValidator oAuthReturnUriValidator;

	public KakaoOAuthService(
			KakaoOAuthProperties properties,
			KakaoOAuthClient kakaoOAuthClient,
			OAuthStateStore oAuthStateStore,
			OAuthUserProvisioner oAuthUserProvisioner,
			OAuthReturnUriValidator oAuthReturnUriValidator) {
		this.properties = properties;
		this.kakaoOAuthClient = kakaoOAuthClient;
		this.oAuthStateStore = oAuthStateStore;
		this.oAuthUserProvisioner = oAuthUserProvisioner;
		this.oAuthReturnUriValidator = oAuthReturnUriValidator;
	}

	public AuthUrlResponse createAuthorizeUrl(String requestedRedirectUri, String requestedReturnUri) {
		ensureConfigured();
		String redirectUri = properties.resolveRedirectUri(requestedRedirectUri);
		String returnUri = oAuthReturnUriValidator.resolveReturnUri(requestedReturnUri, OAuthProvider.KAKAO);
		String state = UUID.randomUUID().toString();
		oAuthStateStore.saveState(state, OAuthProvider.KAKAO, redirectUri, returnUri);
		return new AuthUrlResponse(kakaoOAuthClient.buildAuthorizeUrl(state, redirectUri), state);
	}

	public String buildRedirectBridge(String code, String state) {
		return OAuthRedirectSupport.buildRedirectBridge(oAuthStateStore, code, state);
	}

	public AuthResponse loginWithAuthorizationCode(OAuthCallbackRequest request) {
		ensureConfigured();
		var oauthState = oAuthStateStore.consumeState(request.state());
		OAuthRedirectSupport.assertProvider(oauthState, OAuthProvider.KAKAO);

		var tokenResponse = kakaoOAuthClient.exchangeCodeForToken(request.code(), oauthState.redirectUri());
		if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
			throw new IllegalStateException("Failed to exchange Kakao authorization code");
		}

		KakaoUserResponse kakaoUser = kakaoOAuthClient.fetchUser(tokenResponse.accessToken());
		if (kakaoUser == null || kakaoUser.id() == null) {
			throw new IllegalStateException("Failed to fetch Kakao user profile");
		}

		return oAuthUserProvisioner.issueAuthResponse(
				OAuthProvider.KAKAO, String.valueOf(kakaoUser.id()), resolveNickname(kakaoUser));
	}

	private String resolveNickname(KakaoUserResponse kakaoUser) {
		if (kakaoUser.properties() != null) {
			Object nickname = kakaoUser.properties().get("nickname");
			if (nickname instanceof String value && StringUtils.hasText(value)) {
				return value;
			}
		}
		if (kakaoUser.kakaoAccount() != null) {
			if (kakaoUser.kakaoAccount().profile() != null
					&& StringUtils.hasText(kakaoUser.kakaoAccount().profile().nickname())) {
				return kakaoUser.kakaoAccount().profile().nickname();
			}
			if (StringUtils.hasText(kakaoUser.kakaoAccount().nickname())) {
				return kakaoUser.kakaoAccount().nickname();
			}
		}
		return null;
	}

	private void ensureConfigured() {
		if (!StringUtils.hasText(properties.getRestApiKey())) {
			throw new IllegalStateException("Kakao OAuth is not configured (KAKAO_REST_API_KEY)");
		}
	}
}
