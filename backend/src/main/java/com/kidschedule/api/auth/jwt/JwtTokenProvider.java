package com.kidschedule.api.auth.jwt;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.domain.enums.AccountType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(AuthenticatedUser user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenExpirationMinutes() * 60);

		return Jwts.builder()
				.subject(user.userId().toString())
				.claim("accountType", user.accountType().name())
				.claim("nickname", user.nickname())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(secretKey)
				.compact();
	}

	public AuthenticatedUser parseAccessToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		return new AuthenticatedUser(
				UUID.fromString(claims.getSubject()),
				AccountType.valueOf(claims.get("accountType", String.class)),
				claims.get("nickname", String.class));
	}

	public long getAccessTokenExpirationSeconds() {
		return jwtProperties.getAccessTokenExpirationMinutes() * 60;
	}
}
