package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.auth.oauth.google.GoogleOAuthService;
import com.kidschedule.api.auth.oauth.kakao.KakaoOAuthService;
import com.kidschedule.api.auth.oauth.naver.NaverOAuthService;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.domain.repository.UserOauthLinkRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.OAuthCallbackRequest;
import com.kidschedule.api.web.dto.OAuthLinkResponse;
import com.kidschedule.api.web.dto.OAuthLinksResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthAccountLinkService {

	private final UserRepository userRepository;
	private final UserOauthLinkRepository userOauthLinkRepository;
	private final OAuthStateStore oAuthStateStore;
	private final OAuthUserProvisioner oAuthUserProvisioner;
	private final KakaoOAuthService kakaoOAuthService;
	private final NaverOAuthService naverOAuthService;
	private final GoogleOAuthService googleOAuthService;

	public OAuthAccountLinkService(
			UserRepository userRepository,
			UserOauthLinkRepository userOauthLinkRepository,
			OAuthStateStore oAuthStateStore,
			OAuthUserProvisioner oAuthUserProvisioner,
			KakaoOAuthService kakaoOAuthService,
			NaverOAuthService naverOAuthService,
			GoogleOAuthService googleOAuthService) {
		this.userRepository = userRepository;
		this.userOauthLinkRepository = userOauthLinkRepository;
		this.oAuthStateStore = oAuthStateStore;
		this.oAuthUserProvisioner = oAuthUserProvisioner;
		this.kakaoOAuthService = kakaoOAuthService;
		this.naverOAuthService = naverOAuthService;
		this.googleOAuthService = googleOAuthService;
	}

	@Transactional(readOnly = true)
	public OAuthLinksResponse listLinks(UUID userId) {
		requireUser(userId);
		return toResponse(userId);
	}

	@Transactional(readOnly = true)
	public AuthUrlResponse createLinkUrl(
			UUID userId, OAuthProvider provider, String requestedRedirectUri, String requestedReturnUri) {
		requireUser(userId);
		if (userOauthLinkRepository.findByUserIdAndOauthProvider(userId, provider).isPresent()) {
			throw new IllegalArgumentException(providerLabel(provider) + " 계정이 이미 연결되어 있습니다.");
		}

		return switch (provider) {
			case KAKAO -> kakaoOAuthService.createLinkAuthorizeUrl(userId, requestedRedirectUri, requestedReturnUri);
			case NAVER -> naverOAuthService.createLinkAuthorizeUrl(userId, requestedRedirectUri, requestedReturnUri);
			case GOOGLE -> googleOAuthService.createLinkAuthorizeUrl(userId, requestedRedirectUri, requestedReturnUri);
		};
	}

	@Transactional
	public OAuthLinksResponse completeLink(OAuthProvider provider, OAuthCallbackRequest request) {
		OAuthState oauthState = oAuthStateStore.consumeState(request.state());
		OAuthRedirectSupport.assertProvider(oauthState, provider);
		if (!oauthState.isLinkFlow()) {
			throw new IllegalArgumentException("Invalid link state");
		}

		String oauthSubject = resolveOAuthSubject(provider, request, oauthState);
		oAuthUserProvisioner.linkOAuthToUser(oauthState.linkUserId(), provider, oauthSubject);
		return toResponse(oauthState.linkUserId());
	}

	private String resolveOAuthSubject(OAuthProvider provider, OAuthCallbackRequest request, OAuthState oauthState) {
		return switch (provider) {
			case KAKAO -> kakaoOAuthService.resolveOAuthSubject(request, oauthState);
			case NAVER -> naverOAuthService.resolveOAuthSubject(request, oauthState);
			case GOOGLE -> googleOAuthService.resolveOAuthSubject(request, oauthState);
		};
	}

	private OAuthLinksResponse toResponse(UUID userId) {
		return new OAuthLinksResponse(userOauthLinkRepository.findByUserIdOrderByOauthProviderAsc(userId).stream()
				.map(link -> new OAuthLinkResponse(link.getOauthProvider()))
				.toList());
	}

	private void requireUser(UUID userId) {
		userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	private String providerLabel(OAuthProvider provider) {
		return switch (provider) {
			case KAKAO -> "카카오";
			case NAVER -> "네이버";
			case GOOGLE -> "Google";
		};
	}
}
