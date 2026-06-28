package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.auth.jwt.JwtTokenProvider;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.entity.UserOauthLink;
import com.kidschedule.api.domain.enums.AccountType;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.domain.repository.UserOauthLinkRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.web.dto.AuthResponse;
import com.kidschedule.api.web.dto.AuthUserResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OAuthUserProvisioner {

	private final UserRepository userRepository;
	private final UserOauthLinkRepository userOauthLinkRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public OAuthUserProvisioner(
			UserRepository userRepository,
			UserOauthLinkRepository userOauthLinkRepository,
			JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.userOauthLinkRepository = userOauthLinkRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Transactional
	public AuthResponse issueAuthResponse(OAuthProvider provider, String oauthSubject, String nickname) {
		if (!StringUtils.hasText(oauthSubject)) {
			throw new IllegalStateException("OAuth subject is required");
		}

		String resolvedNickname = StringUtils.hasText(nickname) ? nickname : defaultNickname(provider);
		User user = userOauthLinkRepository
				.findByOauthProviderAndOauthSubject(provider, oauthSubject)
				.map(UserOauthLink::getUser)
				.map(existing -> syncNickname(existing, resolvedNickname, provider))
				.orElseGet(() -> createUserWithLink(provider, oauthSubject, resolvedNickname));

		AuthenticatedUser authenticatedUser =
				new AuthenticatedUser(user.getId(), user.getAccountType(), user.getNickname());
		String accessToken = jwtTokenProvider.createAccessToken(authenticatedUser);

		return new AuthResponse(
				accessToken,
				"Bearer",
				jwtTokenProvider.getAccessTokenExpirationSeconds(),
				new AuthUserResponse(user.getId(), user.getNickname(), user.getAccountType()));
	}

	private User createUserWithLink(OAuthProvider provider, String oauthSubject, String nickname) {
		User user = userRepository.save(new User(nickname, AccountType.ADULT));
		userOauthLinkRepository.save(new UserOauthLink(user, provider, oauthSubject));
		return user;
	}

	@Transactional
	public void linkOAuthToUser(UUID userId, OAuthProvider provider, String oauthSubject) {
		if (!StringUtils.hasText(oauthSubject)) {
			throw new IllegalArgumentException("OAuth subject is required");
		}

		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		userOauthLinkRepository
				.findByOauthProviderAndOauthSubject(provider, oauthSubject)
				.ifPresent(existing -> {
					if (!existing.getUser().getId().equals(userId)) {
						throw new IllegalArgumentException(providerLabel(provider) + " 계정이 이미 다른 사용자에 연결되어 있습니다.");
					}
				});

		if (userOauthLinkRepository.findByUserIdAndOauthProvider(userId, provider).isPresent()) {
			throw new IllegalArgumentException(providerLabel(provider) + " 계정이 이미 연결되어 있습니다.");
		}

		userOauthLinkRepository.save(new UserOauthLink(user, provider, oauthSubject));
	}

	private String providerLabel(OAuthProvider provider) {
		return switch (provider) {
			case KAKAO -> "카카오";
			case NAVER -> "네이버";
			case GOOGLE -> "Google";
		};
	}

	private User syncNickname(User user, String resolvedNickname, OAuthProvider provider) {
		if (isPlaceholderNickname(user.getNickname()) && StringUtils.hasText(resolvedNickname)) {
			String providerDefault = defaultNickname(provider);
			if (!providerDefault.equals(resolvedNickname)) {
				user.setNickname(resolvedNickname);
				return userRepository.save(user);
			}
		}
		return user;
	}

	private boolean isPlaceholderNickname(String nickname) {
		return defaultNickname(OAuthProvider.KAKAO).equals(nickname)
				|| defaultNickname(OAuthProvider.NAVER).equals(nickname)
				|| defaultNickname(OAuthProvider.GOOGLE).equals(nickname);
	}

	private String defaultNickname(OAuthProvider provider) {
		return switch (provider) {
			case KAKAO -> "카카오 사용자";
			case NAVER -> "네이버 사용자";
			case GOOGLE -> "Google 사용자";
		};
	}
}
