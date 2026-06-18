package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;
import java.time.Duration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("!test")
public class RedisOAuthStateStore implements OAuthStateStore {

	private static final String KEY_PREFIX = "oauth:state:";
	private static final String VALUE_SEPARATOR = "\t";
	private static final Duration TTL = Duration.ofMinutes(10);

	private final StringRedisTemplate redisTemplate;

	public RedisOAuthStateStore(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void saveState(String state, OAuthProvider provider, String redirectUri, String returnUri) {
		String value = provider.name() + VALUE_SEPARATOR + redirectUri + VALUE_SEPARATOR + nullToEmpty(returnUri);
		redisTemplate.opsForValue().set(KEY_PREFIX + state, value, TTL);
	}

	@Override
	public OAuthState peekState(String state) {
		return parseState(state, redisTemplate.opsForValue().get(KEY_PREFIX + state));
	}

	@Override
	public OAuthState consumeState(String state) {
		return parseState(state, redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + state));
	}

	private OAuthState parseState(String state, String stored) {
		if (!StringUtils.hasText(state)) {
			throw new IllegalArgumentException("OAuth state is required");
		}
		if (!StringUtils.hasText(stored)) {
			throw new IllegalArgumentException("Invalid or expired OAuth state");
		}

		String[] parts = stored.split(VALUE_SEPARATOR, 3);
		if (parts.length < 2) {
			throw new IllegalArgumentException("Invalid OAuth state payload");
		}

		OAuthProvider provider = OAuthProvider.valueOf(parts[0]);
		String redirectUri = parts[1];
		String returnUri = parts.length > 2 ? emptyToNull(parts[2]) : null;
		return new OAuthState(provider, redirectUri, returnUri);
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private String emptyToNull(String value) {
		return StringUtils.hasText(value) ? value : null;
	}
}
