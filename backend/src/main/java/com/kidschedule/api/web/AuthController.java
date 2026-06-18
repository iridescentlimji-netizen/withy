package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.auth.oauth.kakao.KakaoOAuthService;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUrlResponse;
import com.kidschedule.api.web.dto.AuthUserResponse;
import com.kidschedule.api.web.dto.KakaoCallbackRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final KakaoOAuthService kakaoOAuthService;

	public AuthController(KakaoOAuthService kakaoOAuthService) {
		this.kakaoOAuthService = kakaoOAuthService;
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
	public AuthResponse kakaoCallback(@Valid @RequestBody KakaoCallbackRequest request) {
		return kakaoOAuthService.loginWithAuthorizationCode(request);
	}

	@GetMapping("/me")
	public AuthUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
		return new AuthUserResponse(user.userId(), user.nickname(), user.accountType());
	}
}
