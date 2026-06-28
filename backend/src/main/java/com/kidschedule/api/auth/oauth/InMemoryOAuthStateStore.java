package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("test")
public class InMemoryOAuthStateStore implements OAuthStateStore {

	private final Map<String, OAuthState> states = new ConcurrentHashMap<>();

	@Override
	public void saveState(String state, OAuthProvider provider, String redirectUri, String returnUri, UUID linkUserId) {
		states.put(state, new OAuthState(provider, redirectUri, returnUri, linkUserId));
	}

	@Override
	public OAuthState peekState(String state) {
		if (!StringUtils.hasText(state)) {
			throw new IllegalArgumentException("OAuth state is required");
		}
		OAuthState oauthState = states.get(state);
		if (oauthState == null) {
			throw new IllegalArgumentException("Invalid or expired OAuth state");
		}
		return oauthState;
	}

	@Override
	public OAuthState consumeState(String state) {
		if (!StringUtils.hasText(state)) {
			throw new IllegalArgumentException("OAuth state is required");
		}
		OAuthState oauthState = states.remove(state);
		if (oauthState == null) {
			throw new IllegalArgumentException("Invalid or expired OAuth state");
		}
		return oauthState;
	}
}
