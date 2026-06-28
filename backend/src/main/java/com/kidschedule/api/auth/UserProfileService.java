package com.kidschedule.api.auth;

import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.web.dto.AuthUserResponse;
import com.kidschedule.api.web.dto.UpdateNicknameRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

	private final UserRepository userRepository;

	public UserProfileService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public AuthUserResponse getProfile(UUID userId) {
		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		return new AuthUserResponse(user.getId(), user.getNickname(), user.getAccountType());
	}

	@Transactional
	public AuthUserResponse updateNickname(UUID userId, UpdateNicknameRequest request) {
		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		String trimmed = request.nickname().trim();
		user.setNickname(trimmed);
		return new AuthUserResponse(user.getId(), user.getNickname(), user.getAccountType());
	}
}
