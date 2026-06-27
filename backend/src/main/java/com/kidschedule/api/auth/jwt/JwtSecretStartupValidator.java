package com.kidschedule.api.auth.jwt;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtSecretStartupValidator {

	private static final int MIN_SECRET_LENGTH = 32;
	private static final String INSECURE_DEFAULT_SECRET = "change-me-use-at-least-32-characters-secret-key";

	private final JwtProperties jwtProperties;
	private final Environment environment;

	public JwtSecretStartupValidator(JwtProperties jwtProperties, Environment environment) {
		this.jwtProperties = jwtProperties;
		this.environment = environment;
		validate();
	}

	private void validate() {
		String secret = jwtProperties.getSecret();
		if (!StringUtils.hasText(secret)) {
			throw new IllegalStateException("JWT secret is required (set JWT_SECRET)");
		}
		if (secret.length() < MIN_SECRET_LENGTH) {
			throw new IllegalStateException("JWT secret must be at least " + MIN_SECRET_LENGTH + " characters");
		}
		if (INSECURE_DEFAULT_SECRET.equals(secret)) {
			throw new IllegalStateException("JWT secret must be changed from the default value");
		}
		if (isProductionLikeProfile() && secret.startsWith("local-dev-")) {
			throw new IllegalStateException("JWT secret must not use a local development default in production");
		}
	}

	private boolean isProductionLikeProfile() {
		Set<String> activeProfiles = resolveActiveProfiles();
		return !activeProfiles.contains("local") && !activeProfiles.contains("test");
	}

	private Set<String> resolveActiveProfiles() {
		String[] activeProfiles = environment.getActiveProfiles();
		if (activeProfiles.length > 0) {
			return Arrays.stream(activeProfiles).collect(Collectors.toSet());
		}
		return Arrays.stream(environment.getDefaultProfiles()).collect(Collectors.toSet());
	}
}
