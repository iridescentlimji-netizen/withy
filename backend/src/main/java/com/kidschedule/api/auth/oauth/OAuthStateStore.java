package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;
import java.util.UUID;

public interface OAuthStateStore {

	void saveState(String state, OAuthProvider provider, String redirectUri, String returnUri, UUID linkUserId);

	default void saveState(String state, OAuthProvider provider, String redirectUri, String returnUri) {
		saveState(state, provider, redirectUri, returnUri, null);
	}

	OAuthState peekState(String state);

	OAuthState consumeState(String state);
}
