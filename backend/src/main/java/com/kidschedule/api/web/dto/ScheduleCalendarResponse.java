package com.kidschedule.api.web.dto;

import java.time.LocalDate;
import java.util.Map;

public record ScheduleCalendarResponse(Map<LocalDate, Long> countsByDate) {
}
