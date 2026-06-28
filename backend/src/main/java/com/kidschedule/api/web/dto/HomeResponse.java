package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.MemberRole;
import com.kidschedule.api.domain.enums.ScheduleType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HomeResponse(
		String nickname,
		MemberRole role,
		List<HomeChildCardResponse> children,
		List<HomeReturnPlanResponse> returnPlans) {

	public record HomeChildCardResponse(
			UUID childId,
			String nickname,
			ScheduleSummaryResponse current,
			ScheduleSummaryResponse next,
			long todayScheduleCount) {
	}

	public record ScheduleSummaryResponse(
			UUID scheduleId,
			String title,
			String academyName,
			ScheduleType scheduleType,
			Instant startAt,
			Instant endAt) {
	}

	public record HomeReturnPlanResponse(
			UUID scheduleId,
			UUID childId,
			String childNickname,
			Instant scheduledAt,
			UUID pickupGuardianId,
			String pickupGuardianNickname) {
	}
}
