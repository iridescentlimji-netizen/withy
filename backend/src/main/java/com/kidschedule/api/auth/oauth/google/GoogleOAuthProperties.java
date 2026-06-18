package com.kidschedule.api.auth.oauth.google;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.oauth.google")
public class GoogleOAuthProperties {

	private String clientId = "";
	private String clientSecret = "";
	private String redirectUris = "http://localhost:8080/api/v1/auth/google/redirect";

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}

	public List<String> getAllowedRedirectUris() {
		return Arrays.stream(redirectUris.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
	}

	public String resolveRedirectUri(String requestedRedirectUri) {
		if (!StringUtils.hasText(requestedRedirectUri)) {
			return getAllowedRedirectUris().getFirst();
		}
		if (getAllowedRedirectUris().contains(requestedRedirectUri)) {
			return requestedRedirectUri;
		}
		throw new IllegalArgumentException("Redirect URI is not allowed");
	}
}
