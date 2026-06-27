package com.kidschedule.api.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthOAuthSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getKakaoAuthUrl_rejectsDisallowedReturnUri() throws Exception {
		mockMvc.perform(get("/api/v1/auth/kakao/url")
						.param(
								"redirectUri",
								"http://localhost:8080/api/v1/auth/kakao/redirect")
						.param("returnUri", "exp://evil.example:8081/--/oauth/kakao"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Return URI is not allowed"));
	}

	@Test
	void getKakaoAuthUrl_acceptsAllowedReturnUri() throws Exception {
		mockMvc.perform(get("/api/v1/auth/kakao/url")
						.param(
								"redirectUri",
								"http://localhost:8080/api/v1/auth/kakao/redirect")
						.param("returnUri", "kid-schedule://oauth/kakao"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.authUrl").isNotEmpty());
	}
}
