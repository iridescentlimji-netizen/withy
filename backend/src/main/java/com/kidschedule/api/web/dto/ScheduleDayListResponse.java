package com.kidschedule.api.web.dto;

import java.util.List;

public record ScheduleDayListResponse(
		List<ScheduleResponse> upcoming,
		List<ScheduleResponse> inProgress,
		List<ScheduleResponse> completed) {
}
