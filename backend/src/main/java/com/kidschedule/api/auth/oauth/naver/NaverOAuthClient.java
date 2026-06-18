package com.kidschedule.api.auth.oauth.naver;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class NaverOAuthClient {

	private static final String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
	private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
	private static final String USER_ME_URL = "https://openapi.naver.com/v1/nid/me";

	private final RestClient restClient;
	private final NaverOAuthProperties properties;

	public NaverOAuthClient(NaverOAuthProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.create();
	}

	public String buildAuthorizeUrl(String state, String redirectUri) {
		String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
		return AUTHORIZE_URL
				+ "?response_type=code"
				+ "&client_id=" + properties.getClientId()
				+ "&redirect_uri=" + encodedRedirectUri
				+ "&state=" + state;
	}

	public NaverTokenResponse exchangeCodeForToken(String code, String state, String redirectUri) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", properties.getClientId());
		form.add("client_secret", properties.getClientSecret());
		form.add("code", code);
		form.add("state", state);
		form.add("redirect_uri", redirectUri);

		return restClient.post()
				.uri(TOKEN_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(NaverTokenResponse.class);
	}

	public NaverUserResponse fetchUser(String accessToken) {
		NaverUserEnvelope envelope = restClient.get()
				.uri(USER_ME_URL)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(NaverUserEnvelope.class);
		return envelope == null ? null : envelope.response();
	}
}
