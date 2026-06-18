package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;

public interface OAuthStateStore {

	void saveState(String state, OAuthProvider provider, String redirectUri, String returnUri);

	OAuthState peekState(String state);

	OAuthState consumeState(String state);
}
