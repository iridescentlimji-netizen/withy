package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.schedule.ScheduleService;
import com.kidschedule.api.web.dto.CancelScheduleRequest;
import com.kidschedule.api.web.dto.CreateScheduleRequest;
import com.kidschedule.api.web.dto.ScheduleCalendarResponse;
import com.kidschedule.api.web.dto.ScheduleDayListResponse;
import com.kidschedule.api.web.dto.ScheduleResponse;
import com.kidschedule.api.web.dto.UpdateScheduleRequest;
import com.kidschedule.api.web.dto.UpdateScheduleSeriesRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/families/{familyId}/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ScheduleResponse createSchedule(
			@PathVariable UUID familyId,
			@Valid @RequestBody CreateScheduleRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return scheduleService.createSchedule(familyId, user.userId(), request);
	}

	@GetMapping("/{scheduleId}")
	public ScheduleResponse getSchedule(
			@PathVariable UUID familyId,
			@PathVariable UUID scheduleId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return scheduleService.getSchedule(familyId, scheduleId, user.userId());
	}

	@PutMapping("/{scheduleId}")
	public ScheduleResponse updateSchedule(
			@PathVariable UUID familyId,
			@PathVariable UUID scheduleId,
			@Valid @RequestBody UpdateScheduleRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return scheduleService.updateSchedule(familyId, scheduleId, user.userId(), request);
	}

	@PostMapping("/{scheduleId}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelSchedule(
			@PathVariable UUID familyId,
			@PathVariable UUID scheduleId,
			@AuthenticationPrincipal AuthenticatedUser user,
			@RequestBody(required = false) @Valid CancelScheduleRequest request) {
		scheduleService.cancelSchedule(familyId, scheduleId, user.userId(), request);
	}

	@PutMapping("/series/{seriesId}")
	public ScheduleResponse updateSeries(
			@PathVariable UUID familyId,
			@PathVariable UUID seriesId,
			@Valid @RequestBody UpdateScheduleSeriesRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return scheduleService.updateSeries(familyId, seriesId, user.userId(), request);
	}

	@GetMapping
	public ScheduleDayListResponse listSchedules(
			@PathVariable UUID familyId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) UUID childId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return scheduleService.listSchedulesForDay(familyId, user.userId(), date, childId);
	}

	@GetMapping("/calendar")
	public ScheduleCalendarResponse getCalendar(
			@PathVariable UUID familyId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
			@RequestParam(required = false) UUID childId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return scheduleService.getCalendarCounts(familyId, user.userId(), month, childId);
	}
}
