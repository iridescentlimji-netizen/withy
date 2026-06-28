package com.kidschedule.api.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.auth.jwt.JwtTokenProvider;
import com.kidschedule.api.auth.oauth.OAuthStateStore;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.entity.UserOauthLink;
import com.kidschedule.api.domain.enums.AccountType;
import com.kidschedule.api.domain.enums.OAuthProvider;
import com.kidschedule.api.domain.repository.UserOauthLinkRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OAuthAccountLinkIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserOauthLinkRepository userOauthLinkRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private OAuthStateStore oAuthStateStore;

	private User user;
	private String authHeader;

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User("연결테스트", AccountType.ADULT));
		userOauthLinkRepository.save(new UserOauthLink(user, OAuthProvider.NAVER, "naver-subject-1"));
		String token = jwtTokenProvider.createAccessToken(
				new AuthenticatedUser(user.getId(), AccountType.ADULT, user.getNickname()));
		authHeader = "Bearer " + token;
	}

	@Test
	void listOAuthLinks_returnsConnectedProviders() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me/oauth-links").header("Authorization", authHeader))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.links.length()").value(1))
				.andExpect(jsonPath("$.links[0].provider").value("NAVER"));
	}

	@Test
	void createLinkUrl_rejectsAlreadyLinkedProvider() throws Exception {
		mockMvc.perform(get("/api/v1/auth/naver/link/url")
						.header("Authorization", authHeader)
						.param("returnUri", "kid-schedule://oauth/naver/link"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("네이버 계정이 이미 연결되어 있습니다."));
	}

	@Test
	void createLinkUrl_requiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/auth/kakao/link/url")
						.param("returnUri", "kid-schedule://oauth/kakao/link"))
				.andExpect(status().isForbidden());
	}

	@Test
	void linkCallback_rejectsLoginState() throws Exception {
		String state = UUID.randomUUID().toString();
		oAuthStateStore.saveState(
				state, OAuthProvider.KAKAO, "http://localhost:8080/api/v1/auth/kakao/redirect", "kid-schedule://oauth/kakao");

		mockMvc.perform(post("/api/v1/auth/kakao/link/callback")
						.contentType(APPLICATION_JSON)
						.content("{\"code\":\"code-1\",\"state\":\"" + state + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid link state"));
	}
}
