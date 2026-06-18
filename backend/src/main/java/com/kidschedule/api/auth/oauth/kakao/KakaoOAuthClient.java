package com.kidschedule.api.auth.oauth.kakao;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient {

	private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
	private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
	private static final String USER_ME_URL = "https://kapi.kakao.com/v2/user/me";

	private final RestClient restClient;
	private final KakaoOAuthProperties properties;

	public KakaoOAuthClient(KakaoOAuthProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.create();
	}

	public String buildAuthorizeUrl(String state, String redirectUri) {
		String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
		return AUTHORIZE_URL
				+ "?response_type=code"
				+ "&client_id=" + properties.getRestApiKey()
				+ "&redirect_uri=" + encodedRedirectUri
				+ "&state=" + state;
	}

	public KakaoTokenResponse exchangeCodeForToken(String code, String redirectUri) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "authorization_code");
		form.add("client_id", properties.getRestApiKey());
		form.add("redirect_uri", redirectUri);
		form.add("code", code);
		if (StringUtils.hasText(properties.getClientSecret())) {
			form.add("client_secret", properties.getClientSecret());
		}

		return restClient.post()
				.uri(TOKEN_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(KakaoTokenResponse.class);
	}

	public KakaoUserResponse fetchUser(String accessToken) {
		return restClient.get()
				.uri(USER_ME_URL)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(KakaoUserResponse.class);
	}
}
