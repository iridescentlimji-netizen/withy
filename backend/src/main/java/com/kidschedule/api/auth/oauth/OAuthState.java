package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;

public record OAuthState(OAuthProvider provider, String redirectUri, String returnUri) {
}
