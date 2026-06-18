package com.kidschedule.api.auth.oauth.kakao;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.auth.jwt.JwtTokenProvider;
import com.kidschedule.api.auth.oauth.OAuthState;
import com.kidschedule.api.auth.oauth.OAuthStateStore;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.entity.UserOauthLink;
import com.kidschedule.api.domain.enums.AccountType;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.domain.repository.UserOauthLinkRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.AuthUserResponse;
import com.kidschedule.api.web.dto.KakaoCallbackRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class KakaoOAuthService {

	private final KakaoOAuthProperties properties;
	private final KakaoOAuthClient kakaoOAuthClient;
	private final OAuthStateStore oAuthStateStore;
	private final UserRepository userRepository;
	private final UserOauthLinkRepository userOauthLinkRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public KakaoOAuthService(
			KakaoOAuthProperties properties,
			KakaoOAuthClient kakaoOAuthClient,
			OAuthStateStore oAuthStateStore,
			UserRepository userRepository,
			UserOauthLinkRepository userOauthLinkRepository,
			JwtTokenProvider jwtTokenProvider) {
		this.properties = properties;
		this.kakaoOAuthClient = kakaoOAuthClient;
		this.oAuthStateStore = oAuthStateStore;
		this.userRepository = userRepository;
		this.userOauthLinkRepository = userOauthLinkRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public AuthUrlResponse createAuthorizeUrl(String requestedRedirectUri, String requestedReturnUri) {
		ensureConfigured();
		String redirectUri = properties.resolveRedirectUri(requestedRedirectUri);
		String returnUri = resolveReturnUri(requestedReturnUri);
		String state = UUID.randomUUID().toString();
		oAuthStateStore.saveState(state, OAuthProvider.KAKAO, redirectUri, returnUri);
		return new AuthUrlResponse(kakaoOAuthClient.buildAuthorizeUrl(state, redirectUri), state);
	}

	public String buildRedirectBridge(String code, String state) {
		OAuthState oauthState = oAuthStateStore.peekState(state);
		if (!StringUtils.hasText(oauthState.returnUri())) {
			return """
					<!DOCTYPE html>
					<html lang="ko">
					<body>
					<p>로그인 처리 중입니다. 잠시만 기다려 주세요.</p>
					</body>
					</html>
					""";
		}

		String separator = oauthState.returnUri().contains("?") ? "&" : "?";
		String target = oauthState.returnUri()
				+ separator
				+ "code="
				+ URLEncoder.encode(code, StandardCharsets.UTF_8)
				+ "&state="
				+ URLEncoder.encode(state, StandardCharsets.UTF_8);
		String escapedTarget = target.replace("&", "&amp;").replace("\"", "&quot;");
		String jsTarget = target.replace("\\", "\\\\").replace("'", "\\'");

		return """
				<!DOCTYPE html>
				<html lang="ko">
				<head>
				<meta http-equiv="refresh" content="0;url=%s">
				</head>
				<body>
				<p>앱으로 돌아가는 중...</p>
				<script>window.location.replace('%s');</script>
				</body>
				</html>
				"""
				.formatted(escapedTarget, jsTarget);
	}

	@Transactional
	public AuthResponse loginWithAuthorizationCode(KakaoCallbackRequest request) {
		ensureConfigured();
		var oauthState = oAuthStateStore.consumeState(request.state());

		var tokenResponse = kakaoOAuthClient.exchangeCodeForToken(request.code(), oauthState.redirectUri());
		if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
			throw new IllegalStateException("Failed to exchange Kakao authorization code");
		}

		KakaoUserResponse kakaoUser = kakaoOAuthClient.fetchUser(tokenResponse.accessToken());
		if (kakaoUser == null || kakaoUser.id() == null) {
			throw new IllegalStateException("Failed to fetch Kakao user profile");
		}

		String oauthSubject = String.valueOf(kakaoUser.id());
		User user = userOauthLinkRepository
				.findByOauthProviderAndOauthSubject(OAuthProvider.KAKAO, oauthSubject)
				.map(UserOauthLink::getUser)
				.orElseGet(() -> createUserWithLink(oauthSubject, kakaoUser));

		AuthenticatedUser authenticatedUser =
				new AuthenticatedUser(user.getId(), user.getAccountType(), user.getNickname());
		String accessToken = jwtTokenProvider.createAccessToken(authenticatedUser);

		return new AuthResponse(
				accessToken,
				"Bearer",
				jwtTokenProvider.getAccessTokenExpirationSeconds(),
				new AuthUserResponse(user.getId(), user.getNickname(), user.getAccountType()));
	}

	private User createUserWithLink(String oauthSubject, KakaoUserResponse kakaoUser) {
		User user = userRepository.save(new User(resolveNickname(kakaoUser), AccountType.ADULT));
		userOauthLinkRepository.save(new UserOauthLink(user, OAuthProvider.KAKAO, oauthSubject));
		return user;
	}

	private String resolveNickname(KakaoUserResponse kakaoUser) {
		if (kakaoUser.properties() != null) {
			Object nickname = kakaoUser.properties().get("nickname");
			if (nickname instanceof String value && StringUtils.hasText(value)) {
				return value;
			}
		}
		if (kakaoUser.kakaoAccount() != null) {
			if (kakaoUser.kakaoAccount().profile() != null
					&& StringUtils.hasText(kakaoUser.kakaoAccount().profile().nickname())) {
				return kakaoUser.kakaoAccount().profile().nickname();
			}
			if (StringUtils.hasText(kakaoUser.kakaoAccount().nickname())) {
				return kakaoUser.kakaoAccount().nickname();
			}
		}
		return "카카오 사용자";
	}

	private void ensureConfigured() {
		if (!StringUtils.hasText(properties.getRestApiKey())) {
			throw new IllegalStateException("Kakao OAuth is not configured (KAKAO_REST_API_KEY)");
		}
	}

	private String resolveReturnUri(String requestedReturnUri) {
		if (!StringUtils.hasText(requestedReturnUri)) {
			return null;
		}
		if (requestedReturnUri.startsWith("exp://")
				|| requestedReturnUri.startsWith("kid-schedule://")) {
			return requestedReturnUri;
		}
		throw new IllegalArgumentException("Invalid return URI");
	}
}
