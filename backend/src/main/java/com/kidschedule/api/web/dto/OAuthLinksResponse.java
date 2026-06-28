package com.kidschedule.api.web.dto;

import java.util.List;

public record OAuthLinksResponse(List<OAuthLinkResponse> links) {
}
