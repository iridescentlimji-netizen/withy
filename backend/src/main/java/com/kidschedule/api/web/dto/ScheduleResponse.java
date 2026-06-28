package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.ScheduleStatus;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.enums.SubjectCategory;
import java.time.Instant;
import java.util.UUID;

public record ScheduleResponse(
		UUID id,
		UUID seriesId,
		UUID childId,
		String childNickname,
		UUID academyId,
		String academyName,
		String title,
		String description,
		ScheduleType scheduleType,
		SubjectCategory subjectCategory,
		UUID pickupGuardianId,
		String pickupGuardianNickname,
		Instant startAt,
		Instant endAt,
		ScheduleStatus status,
		boolean cancelled) {
}
