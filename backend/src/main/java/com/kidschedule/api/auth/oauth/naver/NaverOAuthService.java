package com.kidschedule.api.auth.oauth.naver;

import com.kidschedule.api.auth.oauth.OAuthRedirectSupport;
import com.kidschedule.api.auth.oauth.OAuthStateStore;
import com.kidschedule.api.auth.oauth.OAuthUserProvisioner;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.OAuthCallbackRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NaverOAuthService {

	private final NaverOAuthProperties properties;
	private final NaverOAuthClient naverOAuthClient;
	private final OAuthStateStore oAuthStateStore;
	private final OAuthUserProvisioner oAuthUserProvisioner;

	public NaverOAuthService(
			NaverOAuthProperties properties,
			NaverOAuthClient naverOAuthClient,
			OAuthStateStore oAuthStateStore,
			OAuthUserProvisioner oAuthUserProvisioner) {
		this.properties = properties;
		this.naverOAuthClient = naverOAuthClient;
		this.oAuthStateStore = oAuthStateStore;
		this.oAuthUserProvisioner = oAuthUserProvisioner;
	}

	public AuthUrlResponse createAuthorizeUrl(String requestedRedirectUri, String requestedReturnUri) {
		ensureConfigured();
		String redirectUri = properties.resolveRedirectUri(requestedRedirectUri);
		String returnUri = OAuthRedirectSupport.resolveReturnUri(requestedReturnUri);
		String state = UUID.randomUUID().toString();
		oAuthStateStore.saveState(state, OAuthProvider.NAVER, redirectUri, returnUri);
		return new AuthUrlResponse(naverOAuthClient.buildAuthorizeUrl(state, redirectUri), state);
	}

	public String buildRedirectBridge(String code, String state) {
		return OAuthRedirectSupport.buildRedirectBridge(oAuthStateStore, code, state);
	}

	@Transactional
	public AuthResponse loginWithAuthorizationCode(OAuthCallbackRequest request) {
		ensureConfigured();
		var oauthState = oAuthStateStore.consumeState(request.state());

		var tokenResponse =
				naverOAuthClient.exchangeCodeForToken(request.code(), request.state(), oauthState.redirectUri());
		if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
			throw new IllegalStateException("Failed to exchange Naver authorization code");
		}

		NaverUserResponse naverUser = naverOAuthClient.fetchUser(tokenResponse.accessToken());
		if (naverUser == null || !StringUtils.hasText(naverUser.id())) {
			throw new IllegalStateException("Failed to fetch Naver user profile");
		}

		return oAuthUserProvisioner.issueAuthResponse(
				OAuthProvider.NAVER, naverUser.id(), resolveNickname(naverUser));
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
