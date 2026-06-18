package com.kidschedule.api.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	private String secret = "change-me-use-at-least-32-characters-secret-key";
	private long accessTokenExpirationMinutes = 60;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getAccessTokenExpirationMinutes() {
		return accessTokenExpirationMinutes;
	}

	public void setAccessTokenExpirationMinutes(long accessTokenExpirationMinutes) {
		this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
	}
}
