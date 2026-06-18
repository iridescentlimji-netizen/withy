package com.kidschedule.api.auth.oauth.kakao;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.oauth.kakao")
public class KakaoOAuthProperties {

	private String restApiKey = "";
	private String clientSecret = "";
	private String redirectUris = "http://localhost:8080/api/v1/auth/kakao/redirect";

	public String getRestApiKey() {
		return restApiKey;
	}

	public void setRestApiKey(String restApiKey) {
		this.restApiKey = restApiKey;
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

	public String getDefaultRedirectUri() {
		List<String> allowed = getAllowedRedirectUris();
		if (allowed.isEmpty()) {
			throw new IllegalStateException("At least one Kakao redirect URI must be configured");
		}
		return allowed.getFirst();
	}

	public String resolveRedirectUri(String requestedRedirectUri) {
		if (!StringUtils.hasText(requestedRedirectUri)) {
			return getDefaultRedirectUri();
		}
		if (getAllowedRedirectUris().contains(requestedRedirectUri)) {
			return requestedRedirectUri;
		}
		throw new IllegalArgumentException("Redirect URI is not allowed");
	}
}
