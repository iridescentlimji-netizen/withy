package com.kidschedule.api.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.auth.jwt.JwtTokenProvider;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.enums.AccountType;
import com.kidschedule.api.domain.enums.SubjectCategory;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.web.dto.CreateAcademyRequest;
import com.kidschedule.api.web.dto.CreateFamilyRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcademyApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	private String authHeader;
	private UUID familyId;

	@BeforeEach
	void setUp() throws Exception {
		User user = userRepository.save(new User("테스트부모", AccountType.ADULT));
		String token = jwtTokenProvider.createAccessToken(
				new AuthenticatedUser(user.getId(), AccountType.ADULT, user.getNickname()));
		authHeader = "Bearer " + token;

		MvcResult familyResult = mockMvc.perform(post("/api/v1/families")
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CreateFamilyRequest("테스트 가족"))))
				.andExpect(status().isCreated())
				.andReturn();
		familyId = UUID.fromString(objectMapper.readTree(familyResult.getResponse().getContentAsString())
				.get("id")
				.asText());
	}

	@Test
	void searchAcademyIgnoresSpaces() throws Exception {
		CreateAcademyRequest request =
				new CreateAcademyRequest("이문 태권도", "02-1234-5678", SubjectCategory.PE, null);

		mockMvc.perform(post("/api/v1/families/{familyId}/academies", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/families/{familyId}/academies", familyId)
						.header("Authorization", authHeader)
						.param("query", "이문태권도"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("이문 태권도"));
	}

	@Test
	void createAcademyRejectsDuplicateNormalizedName() throws Exception {
		CreateAcademyRequest request =
				new CreateAcademyRequest("이문 태권도", null, SubjectCategory.PE, null);

		mockMvc.perform(post("/api/v1/families/{familyId}/academies", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/families/{familyId}/academies", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new CreateAcademyRequest("이문태권도", null, SubjectCategory.PE, null))))
				.andExpect(status().isBadRequest());
	}
}
