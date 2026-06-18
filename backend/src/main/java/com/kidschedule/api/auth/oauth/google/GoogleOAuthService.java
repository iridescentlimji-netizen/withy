package com.kidschedule.api.auth.oauth.google;

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
public class GoogleOAuthService {

	private final GoogleOAuthProperties properties;
	private final GoogleOAuthClient googleOAuthClient;
	private final OAuthStateStore oAuthStateStore;
	private final OAuthUserProvisioner oAuthUserProvisioner;

	public GoogleOAuthService(
			GoogleOAuthProperties properties,
			GoogleOAuthClient googleOAuthClient,
			OAuthStateStore oAuthStateStore,
			OAuthUserProvisioner oAuthUserProvisioner) {
		this.properties = properties;
		this.googleOAuthClient = googleOAuthClient;
		this.oAuthStateStore = oAuthStateStore;
		this.oAuthUserProvisioner = oAuthUserProvisioner;
	}

	public AuthUrlResponse createAuthorizeUrl(String requestedRedirectUri, String requestedReturnUri) {
		ensureConfigured();
		String redirectUri = properties.resolveRedirectUri(requestedRedirectUri);
		String returnUri = OAuthRedirectSupport.resolveReturnUri(requestedReturnUri);
		String state = UUID.randomUUID().toString();
		oAuthStateStore.saveState(state, OAuthProvider.GOOGLE, redirectUri, returnUri);
		return new AuthUrlResponse(googleOAuthClient.buildAuthorizeUrl(state, redirectUri), state);
	}

	public String buildRedirectBridge(String code, String state) {
		return OAuthRedirectSupport.buildRedirectBridge(oAuthStateStore, code, state);
	}

	@Transactional
	public AuthResponse loginWithAuthorizationCode(OAuthCallbackRequest request) {
		ensureConfigured();
		var oauthState = oAuthStateStore.consumeState(request.state());

		var tokenResponse = googleOAuthClient.exchangeCodeForToken(request.code(), oauthState.redirectUri());
		if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
			throw new IllegalStateException("Failed to exchange Google authorization code");
		}

		GoogleUserResponse googleUser = googleOAuthClient.fetchUser(tokenResponse.accessToken());
		if (googleUser == null || !StringUtils.hasText(googleUser.sub())) {
			throw new IllegalStateException("Failed to fetch Google user profile");
		}

		return oAuthUserProvisioner.issueAuthResponse(
				OAuthProvider.GOOGLE, googleUser.sub(), googleUser.name());
	}

	private void ensureConfigured() {
		if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
			throw new IllegalStateException("Google OAuth is not configured (GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET)");
		}
	}
}
