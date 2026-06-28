package com.kidschedule.api.auth.oauth.naver;

import com.kidschedule.api.auth.oauth.OAuthRedirectSupport;
import com.kidschedule.api.auth.oauth.OAuthReturnUriValidator;
import com.kidschedule.api.auth.oauth.OAuthStateStore;
import com.kidschedule.api.auth.oauth.OAuthUserProvisioner;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.OAuthCallbackRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NaverOAuthService {

	private final NaverOAuthProperties properties;
	private final NaverOAuthClient naverOAuthClient;
	private final OAuthStateStore oAuthStateStore;
	private final OAuthUserProvisioner oAuthUserProvisioner;
	private final OAuthReturnUriValidator oAuthReturnUriValidator;

	public NaverOAuthService(
			NaverOAuthProperties properties,
			NaverOAuthClient naverOAuthClient,
			OAuthStateStore oAuthStateStore,
			OAuthUserProvisioner oAuthUserProvisioner,
			OAuthReturnUriValidator oAuthReturnUriValidator) {
		this.properties = properties;
		this.naverOAuthClient = naverOAuthClient;
		this.oAuthStateStore = oAuthStateStore;
		this.oAuthUserProvisioner = oAuthUserProvisioner;
		this.oAuthReturnUriValidator = oAuthReturnUriValidator;
	}

	public AuthUrlResponse createAuthorizeUrl(String requestedRedirectUri, String requestedReturnUri) {
		return createAuthorizeUrl(null, requestedRedirectUri, requestedReturnUri);
	}

	public AuthUrlResponse createLinkAuthorizeUrl(
			java.util.UUID userId, String requestedRedirectUri, String requestedReturnUri) {
		return createAuthorizeUrl(userId, requestedRedirectUri, requestedReturnUri);
	}

	private AuthUrlResponse createAuthorizeUrl(
			java.util.UUID linkUserId, String requestedRedirectUri, String requestedReturnUri) {
		ensureConfigured();
		String redirectUri = properties.resolveRedirectUri(requestedRedirectUri);
		String returnUri = oAuthReturnUriValidator.resolveReturnUri(requestedReturnUri, OAuthProvider.NAVER);
		String state = UUID.randomUUID().toString();
		oAuthStateStore.saveState(state, OAuthProvider.NAVER, redirectUri, returnUri, linkUserId);
		return new AuthUrlResponse(naverOAuthClient.buildAuthorizeUrl(state, redirectUri), state);
	}

	public String buildRedirectBridge(String code, String state) {
		return OAuthRedirectSupport.buildRedirectBridge(oAuthStateStore, code, state);
	}

	public AuthResponse loginWithAuthorizationCode(OAuthCallbackRequest request) {
		ensureConfigured();
		var oauthState = oAuthStateStore.consumeState(request.state());
		OAuthRedirectSupport.assertProvider(oauthState, OAuthProvider.NAVER);
		if (oauthState.isLinkFlow()) {
			throw new IllegalArgumentException("Invalid login state");
		}

		NaverUserResponse naverUser = fetchNaverUser(request, oauthState);
		return oAuthUserProvisioner.issueAuthResponse(
				OAuthProvider.NAVER, naverUser.id(), resolveNickname(naverUser));
	}

	public String resolveOAuthSubject(OAuthCallbackRequest request, com.kidschedule.api.auth.oauth.OAuthState oauthState) {
		NaverUserResponse naverUser = fetchNaverUser(request, oauthState);
		return naverUser.id();
	}

	private NaverUserResponse fetchNaverUser(OAuthCallbackRequest request, com.kidschedule.api.auth.oauth.OAuthState oauthState) {
		ensureConfigured();
		var tokenResponse =
				naverOAuthClient.exchangeCodeForToken(request.code(), request.state(), oauthState.redirectUri());
		if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
			throw new IllegalStateException("Failed to exchange Naver authorization code");
		}

		NaverUserResponse naverUser = naverOAuthClient.fetchUser(tokenResponse.accessToken());
		if (naverUser == null || !StringUtils.hasText(naverUser.id())) {
			throw new IllegalStateException("Failed to fetch Naver user profile");
		}
		return naverUser;
	}

	private String resolveNickname(NaverUserResponse naverUser) {
		if (StringUtils.hasText(naverUser.nickname())) {
			return naverUser.nickname();
		}
		if (StringUtils.hasText(naverUser.name())) {
			return naverUser.name();
		}
		return null;
	}

	private void ensureConfigured() {
		if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
			throw new IllegalStateException("Naver OAuth is not configured (NAVER_CLIENT_ID/NAVER_CLIENT_SECRET)");
		}
	}
}
