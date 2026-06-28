package com.kidschedule.api.schedule;

import com.kidschedule.api.domain.enums.RecurrenceType;
import com.kidschedule.api.domain.enums.ScheduleStatus;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ScheduleTimeSupport {

	public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private ScheduleTimeSupport() {
	}

	public static ScheduleStatus resolveStatus(Instant startAt, Instant endAt, Instant now) {
		if (!now.isBefore(endAt)) {
			return ScheduleStatus.COMPLETED;
		}
		if (!now.isBefore(startAt)) {
			return ScheduleStatus.IN_PROGRESS;
		}
		return ScheduleStatus.UPCOMING;
	}

	public static short toDaysOfWeekBitmask(Set<DayOfWeek> days) {
		short mask = 0;
		for (DayOfWeek day : days) {
			mask |= dayBitmask(day);
		}
		return mask;
	}

	public static Set<DayOfWeek> fromDaysOfWeekBitmask(short mask) {
		Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
		for (DayOfWeek day : DayOfWeek.values()) {
			if ((mask & dayBitmask(day)) != 0) {
				days.add(day);
			}
		}
		return days;
	}

	public static List<LocalDate> generateDates(
			RecurrenceType recurrenceType,
			LocalDate effectiveFrom,
			LocalDate effectiveUntil,
			LocalDate anchorDate,
			Set<DayOfWeek> daysOfWeek,
			int dayOfMonth,
			int horizonWeeks) {
		LocalDate end = effectiveUntil != null ? effectiveUntil : effectiveFrom.plusWeeks(horizonWeeks);
		if (end.isBefore(effectiveFrom)) {
			return List.of();
		}

		return switch (recurrenceType) {
			case NONE -> List.of();
			case WEEKLY -> generateWeeklyDates(effectiveFrom, end, daysOfWeek);
			case BIWEEKLY -> generateBiweeklyDates(effectiveFrom, end, anchorDate, daysOfWeek);
			case MONTHLY -> generateMonthlyDates(effectiveFrom, end, dayOfMonth);
		};
	}

	public static Instant toInstant(LocalDate date, LocalTime time) {
		return ZonedDateTime.of(date, time, ZONE).toInstant();
	}

	private static List<LocalDate> generateWeeklyDates(LocalDate from, LocalDate to, Set<DayOfWeek> daysOfWeek) {
		List<LocalDate> dates = new ArrayList<>();
		LocalDate cursor = from;
		while (!cursor.isAfter(to)) {
			if (daysOfWeek.contains(cursor.getDayOfWeek())) {
				dates.add(cursor);
			}
			cursor = cursor.plusDays(1);
		}
		return dates;
	}

	private static List<LocalDate> generateBiweeklyDates(
			LocalDate from, LocalDate to, LocalDate anchorDate, Set<DayOfWeek> daysOfWeek) {
		LocalDate anchor = anchorDate != null ? anchorDate : from;
		List<LocalDate> dates = new ArrayList<>();
		LocalDate cursor = from;
		while (!cursor.isAfter(to)) {
			if (daysOfWeek.contains(cursor.getDayOfWeek()) && isSameBiweeklyWeek(anchor, cursor)) {
				dates.add(cursor);
			}
			cursor = cursor.plusDays(1);
		}
		return dates;
	}

	private static boolean isSameBiweeklyWeek(LocalDate anchor, LocalDate date) {
		long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
				anchor.with(DayOfWeek.MONDAY), date.with(DayOfWeek.MONDAY));
		return weeksBetween % 2 == 0;
	}

	private static List<LocalDate> generateMonthlyDates(LocalDate from, LocalDate to, int dayOfMonth) {
		List<LocalDate> dates = new ArrayList<>();
		LocalDate cursor = from.withDayOfMonth(1);
		while (!cursor.isAfter(to)) {
			if (dayOfMonth <= cursor.lengthOfMonth()) {
				LocalDate candidate = cursor.withDayOfMonth(dayOfMonth);
				if (!candidate.isBefore(from) && !candidate.isAfter(to)) {
					dates.add(candidate);
				}
			}
			cursor = cursor.plusMonths(1);
		}
		return dates;
	}

	private static short dayBitmask(DayOfWeek day) {
		return (short) (1 << (day.getValue() - 1));
	}
}
