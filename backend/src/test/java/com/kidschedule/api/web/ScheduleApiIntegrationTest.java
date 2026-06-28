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
import com.kidschedule.api.domain.enums.RecurrenceType;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.schedule.ScheduleTimeSupport;
import com.kidschedule.api.web.dto.CancelScheduleRequest;
import com.kidschedule.api.web.dto.CreateChildRequest;
import com.kidschedule.api.web.dto.CreateFamilyRequest;
import com.kidschedule.api.web.dto.CreateScheduleRequest;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
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
class ScheduleApiIntegrationTest {

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
	private UUID childId;

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

		MvcResult childResult = mockMvc.perform(post("/api/v1/families/{familyId}/children", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CreateChildRequest("민수", (short) 2018))))
				.andExpect(status().isCreated())
				.andReturn();
		childId = UUID.fromString(objectMapper.readTree(childResult.getResponse().getContentAsString())
				.get("id")
				.asText());
	}

	@Test
	void createAndListOneTimeSchedule() throws Exception {
		ZonedDateTime startKst = ZonedDateTime.now(ScheduleTimeSupport.ZONE).plusHours(1).truncatedTo(ChronoUnit.SECONDS);
		Instant startAt = startKst.toInstant();
		Instant endAt = startAt.plus(1, ChronoUnit.HOURS);
		String date = startKst.toLocalDate().toString();

		CreateScheduleRequest request = new CreateScheduleRequest(
				childId,
				"피아노",
				null,
				ScheduleType.ACTIVITY,
				null,
				null,
				null,
				RecurrenceType.NONE,
				startAt,
				endAt,
				null,
				null,
				null,
				null,
				null,
				null,
				null);

		mockMvc.perform(post("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("피아노"))
				.andExpect(jsonPath("$.scheduleType").value("ACTIVITY"));

		mockMvc.perform(get("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.param("date", date))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.upcoming.length()").value(1));
	}

	@Test
	void cancelSchedule() throws Exception {
		ZonedDateTime startKst = ZonedDateTime.now(ScheduleTimeSupport.ZONE).plusHours(2).truncatedTo(ChronoUnit.SECONDS);
		Instant startAt = startKst.toInstant();
		Instant endAt = startAt.plus(1, ChronoUnit.HOURS);
		String date = startKst.toLocalDate().toString();

		CreateScheduleRequest request = new CreateScheduleRequest(
				childId,
				"귀가",
				null,
				ScheduleType.PICKUP,
				null,
				null,
				null,
				RecurrenceType.NONE,
				startAt,
				endAt,
				null,
				null,
				null,
				null,
				null,
				null,
				null);

		MvcResult createResult = mockMvc.perform(post("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn();

		UUID scheduleId = UUID.fromString(objectMapper
				.readTree(createResult.getResponse().getContentAsString())
				.get("id")
				.asText());

		mockMvc.perform(post("/api/v1/families/{familyId}/schedules/{scheduleId}/cancel", familyId, scheduleId)
						.header("Authorization", authHeader))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.param("date", date))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.upcoming.length()").value(0));
	}

	@Test
	void cancelRecurringScheduleFutureScopeEndsSeriesAndUnmaterializedDates() throws Exception {
		LocalDate effectiveFrom = LocalDate.of(2026, 6, 30);
		LocalDate cancelDate = LocalDate.of(2026, 9, 22);

		CreateScheduleRequest request = new CreateScheduleRequest(
				childId,
				"태권도",
				null,
				ScheduleType.ACTIVITY,
				null,
				null,
				null,
				RecurrenceType.WEEKLY,
				null,
				null,
				LocalTime.of(15, 0),
				LocalTime.of(16, 0),
				effectiveFrom,
				null,
				effectiveFrom,
				List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
				null);

		MvcResult createResult = mockMvc.perform(post("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn();

		UUID scheduleId = UUID.fromString(objectMapper
				.readTree(createResult.getResponse().getContentAsString())
				.get("id")
				.asText());

		mockMvc.perform(get("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.param("date", cancelDate.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.upcoming.length()").value(1));

		mockMvc.perform(post("/api/v1/families/{familyId}/schedules/{scheduleId}/cancel", familyId, scheduleId)
						.header("Authorization", authHeader)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CancelScheduleRequest(
								com.kidschedule.api.domain.enums.CancelScope.FUTURE))))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.param("date", cancelDate.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.upcoming.length()").value(0));

		mockMvc.perform(get("/api/v1/families/{familyId}/schedules", familyId)
						.header("Authorization", authHeader)
						.param("date", "2026-10-15"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.upcoming.length()").value(0))
				.andExpect(jsonPath("$.inProgress.length()").value(0))
				.andExpect(jsonPath("$.completed.length()").value(0));
	}
}
