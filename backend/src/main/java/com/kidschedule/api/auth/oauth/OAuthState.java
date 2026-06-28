package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;
import java.util.UUID;

public record OAuthState(OAuthProvider provider, String redirectUri, String returnUri, UUID linkUserId) {

	public OAuthState(OAuthProvider provider, String redirectUri, String returnUri) {
		this(provider, redirectUri, returnUri, null);
	}

	public boolean isLinkFlow() {
		return linkUserId != null;
	}
}
