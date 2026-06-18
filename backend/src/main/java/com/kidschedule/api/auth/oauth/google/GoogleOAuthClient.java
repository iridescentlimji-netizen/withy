package com.kidschedule.api.auth.oauth.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthClient {

	private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
	private static final String SCOPES = "openid profile";

	private final RestClient restClient;
	private final GoogleOAuthProperties properties;

	public GoogleOAuthClient(GoogleOAuthProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.create();
	}

	public String buildAuthorizeUrl(String state, String redirectUri) {
		String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
		String encodedScopes = URLEncoder.encode(SCOPES, StandardCharsets.UTF_8);
		return AUTHORIZE_URL
				+ "?response_type=code"
				+ "&client_id=" + properties.getClientId()
				+ "&redirect_uri=" + encodedRedirectUri
				+ "&scope=" + encodedScopes
				+ "&state=" + state;
	}

	public GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", properties.getClientId());
		form.add("client_secret", properties.getClientSecret());
		form.add("redirect_uri", redirectUri);
		form.add("code", code);

		return restClient.post()
				.uri(TOKEN_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(GoogleTokenResponse.class);
	}

	public GoogleUserResponse fetchUser(String accessToken) {
		return restClient.get()
				.uri(USER_INFO_URL)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(GoogleUserResponse.class);
	}
}
