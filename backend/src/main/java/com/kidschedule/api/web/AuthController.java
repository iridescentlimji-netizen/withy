package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.auth.UserProfileService;
import com.kidschedule.api.auth.oauth.OAuthAccountLinkService;
import com.kidschedule.api.auth.oauth.google.GoogleOAuthService;
import com.kidschedule.api.auth.oauth.kakao.KakaoOAuthService;
import com.kidschedule.api.auth.oauth.naver.NaverOAuthService;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.AuthUserResponse;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.web.dto.OAuthCallbackRequest;
import com.kidschedule.api.web.dto.OAuthLinksResponse;
import com.kidschedule.api.web.dto.UpdateNicknameRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final KakaoOAuthService kakaoOAuthService;
	private final NaverOAuthService naverOAuthService;
	private final GoogleOAuthService googleOAuthService;
	private final UserProfileService userProfileService;
	private final OAuthAccountLinkService oAuthAccountLinkService;

	public AuthController(
			KakaoOAuthService kakaoOAuthService,
			NaverOAuthService naverOAuthService,
			GoogleOAuthService googleOAuthService,
			UserProfileService userProfileService,
			OAuthAccountLinkService oAuthAccountLinkService) {
		this.kakaoOAuthService = kakaoOAuthService;
		this.naverOAuthService = naverOAuthService;
		this.googleOAuthService = googleOAuthService;
		this.userProfileService = userProfileService;
		this.oAuthAccountLinkService = oAuthAccountLinkService;
	}

	@GetMapping("/kakao/url")
	public AuthUrlResponse getKakaoAuthUrl(
			@RequestParam(required = false) String redirectUri, @RequestParam(required = false) String returnUri) {
		return kakaoOAuthService.createAuthorizeUrl(redirectUri, returnUri);
	}

	@GetMapping(value = "/kakao/redirect", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> kakaoRedirect(@RequestParam String code, @RequestParam String state) {
		return ResponseEntity.ok(kakaoOAuthService.buildRedirectBridge(code, state));
	}

	@PostMapping("/kakao/callback")
	public AuthResponse kakaoCallback(@Valid @RequestBody OAuthCallbackRequest request) {
		return kakaoOAuthService.loginWithAuthorizationCode(request);
	}

	@GetMapping("/naver/url")
	public AuthUrlResponse getNaverAuthUrl(
			@RequestParam(required = false) String redirectUri, @RequestParam(required = false) String returnUri) {
		return naverOAuthService.createAuthorizeUrl(redirectUri, returnUri);
	}

	@GetMapping(value = "/naver/redirect", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> naverRedirect(@RequestParam String code, @RequestParam String state) {
		return ResponseEntity.ok(naverOAuthService.buildRedirectBridge(code, state));
	}

	@PostMapping("/naver/callback")
	public AuthResponse naverCallback(@Valid @RequestBody OAuthCallbackRequest request) {
		return naverOAuthService.loginWithAuthorizationCode(request);
	}

	@GetMapping("/google/url")
	public AuthUrlResponse getGoogleAuthUrl(
			@RequestParam(required = false) String redirectUri, @RequestParam(required = false) String returnUri) {
		return googleOAuthService.createAuthorizeUrl(redirectUri, returnUri);
	}

	@GetMapping(value = "/google/redirect", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> googleRedirect(@RequestParam String code, @RequestParam String state) {
		return ResponseEntity.ok(googleOAuthService.buildRedirectBridge(code, state));
	}

	@PostMapping("/google/callback")
	public AuthResponse googleCallback(@Valid @RequestBody OAuthCallbackRequest request) {
		return googleOAuthService.loginWithAuthorizationCode(request);
	}

	@GetMapping("/me")
	public AuthUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
		return userProfileService.getProfile(user.userId());
	}

	@PatchMapping("/me")
	public AuthUserResponse updateMe(
			@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody UpdateNicknameRequest request) {
		return userProfileService.updateNickname(user.userId(), request);
	}

	@GetMapping("/me/oauth-links")
	public OAuthLinksResponse listOAuthLinks(@AuthenticationPrincipal AuthenticatedUser user) {
		return oAuthAccountLinkService.listLinks(user.userId());
	}

	@GetMapping("/{provider}/link/url")
	public AuthUrlResponse getOAuthLinkUrl(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable OAuthProvider provider,
			@RequestParam(required = false) String redirectUri,
			@RequestParam(required = false) String returnUri) {
		return oAuthAccountLinkService.createLinkUrl(user.userId(), provider, redirectUri, returnUri);
	}

	@PostMapping("/{provider}/link/callback")
	public OAuthLinksResponse oauthLinkCallback(
			@PathVariable OAuthProvider provider, @Valid @RequestBody OAuthCallbackRequest request) {
		return oAuthAccountLinkService.completeLink(provider, request);
	}
}
