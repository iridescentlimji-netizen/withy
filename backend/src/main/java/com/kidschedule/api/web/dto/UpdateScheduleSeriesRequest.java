package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.enums.SubjectCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateScheduleSeriesRequest(
		@NotBlank @Size(max = 100) String title,
		@Size(max = 500) String description,
		@NotNull ScheduleType scheduleType,
		UUID academyId,
		SubjectCategory subjectCategory,
		UUID pickupGuardianId,
		@NotNull LocalTime startTime,
		@NotNull LocalTime endTime) {
}
