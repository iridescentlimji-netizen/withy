package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.RecurrenceType;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.enums.SubjectCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateScheduleRequest(
		@NotNull UUID childId,
		@NotBlank @Size(max = 100) String title,
		@Size(max = 500) String description,
		@NotNull ScheduleType scheduleType,
		UUID academyId,
		SubjectCategory subjectCategory,
		UUID pickupGuardianId,
		@NotNull RecurrenceType recurrence,
		Instant startAt,
		Instant endAt,
		LocalTime startTime,
		LocalTime endTime,
		LocalDate effectiveFrom,
		LocalDate effectiveUntil,
		LocalDate anchorDate,
		List<DayOfWeek> daysOfWeek,
		Integer dayOfMonth) {
}
