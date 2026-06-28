package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.OAuthProvider;

public record OAuthLinkResponse(OAuthProvider provider) {
}
