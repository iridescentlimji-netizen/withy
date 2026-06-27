package com.kidschedule.api.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class JwtSecretStartupValidatorTest {

	@Mock
	private Environment environment;

	@Test
	void rejectsDefaultSecret() {
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setSecret("change-me-use-at-least-32-characters-secret-key");

		assertThrows(IllegalStateException.class, () -> new JwtSecretStartupValidator(jwtProperties, environment));
	}

	@Test
	void rejectsShortSecret() {
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setSecret("too-short");

		assertThrows(IllegalStateException.class, () -> new JwtSecretStartupValidator(jwtProperties, environment));
	}

	@Test
	void allowsLocalSecretInLocalProfile() {
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setSecret("local-dev-jwt-secret-key-at-least-32-chars");
		when(environment.getActiveProfiles()).thenReturn(new String[] {"local"});

		assertDoesNotThrow(() -> new JwtSecretStartupValidator(jwtProperties, environment));
	}

	@Test
	void rejectsLocalDevSecretInProductionLikeProfile() {
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setSecret("local-dev-jwt-secret-key-at-least-32-chars");
		when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});

		assertThrows(IllegalStateException.class, () -> new JwtSecretStartupValidator(jwtProperties, environment));
	}

	@Test
	void allowsTestSecretInTestProfile() {
		JwtProperties jwtProperties = new JwtProperties();
		jwtProperties.setSecret("test-jwt-secret-key-at-least-32-characters-long");
		when(environment.getActiveProfiles()).thenReturn(new String[] {"test"});

		assertDoesNotThrow(() -> new JwtSecretStartupValidator(jwtProperties, environment));
	}
}
