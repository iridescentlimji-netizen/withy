package com.kidschedule.api.schedule;

import com.kidschedule.api.config.ScheduleProperties;
import com.kidschedule.api.domain.entity.Schedule;
import com.kidschedule.api.domain.entity.ScheduleSeries;
import com.kidschedule.api.domain.repository.ScheduleRepository;
import com.kidschedule.api.domain.repository.ScheduleSeriesRepository;
import jakarta.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleSeriesMaterializer {

	private static final int VIEW_BUFFER_WEEKS = 4;

	private final ScheduleRepository scheduleRepository;
	private final ScheduleSeriesRepository scheduleSeriesRepository;
	private final ScheduleProperties scheduleProperties;
	private final EntityManager entityManager;

	public ScheduleSeriesMaterializer(
			ScheduleRepository scheduleRepository,
			ScheduleSeriesRepository scheduleSeriesRepository,
			ScheduleProperties scheduleProperties,
			EntityManager entityManager) {
		this.scheduleRepository = scheduleRepository;
		this.scheduleSeriesRepository = scheduleSeriesRepository;
		this.scheduleProperties = scheduleProperties;
		this.entityManager = entityManager;
	}

	@Transactional
	public void ensureMaterializedThrough(UUID familyId, LocalDate throughDate) {
		for (ScheduleSeries series : scheduleSeriesRepository.findByFamilyId(familyId)) {
			materializeThrough(series.getId(), throughDate);
		}
	}

	@Transactional
	public void materializeInitial(ScheduleSeries series) {
		ScheduleSeries lockedSeries = lockSeries(series.getId());
		LocalDate end = resolveInitialEnd(lockedSeries);
		materializeRange(lockedSeries, lockedSeries.getEffectiveFrom(), end);
	}

	private void materializeThrough(UUID seriesId, LocalDate throughDate) {
		ScheduleSeries lockedSeries = lockSeries(seriesId);
		LocalDate targetEnd = resolveTargetEnd(lockedSeries, throughDate);
		Optional<Instant> latestStartAt = scheduleRepository.findLatestStartAtBySeriesId(seriesId);
		LocalDate fromDate = latestStartAt
				.map(instant -> LocalDate.ofInstant(instant, ScheduleTimeSupport.ZONE).plusDays(1))
				.orElse(lockedSeries.getEffectiveFrom());

		if (fromDate.isAfter(targetEnd)) {
			return;
		}

		materializeRange(lockedSeries, fromDate, targetEnd);
	}

	private ScheduleSeries lockSeries(UUID seriesId) {
		return scheduleSeriesRepository
				.findByIdForUpdate(seriesId)
				.orElseThrow(() -> new IllegalArgumentException("Schedule series not found"));
	}

	private LocalDate resolveInitialEnd(ScheduleSeries series) {
		if (series.getEffectiveUntil() != null) {
			return series.getEffectiveUntil();
		}
		return series.getEffectiveFrom().plusWeeks(scheduleProperties.getHorizonWeeks());
	}

	private LocalDate resolveTargetEnd(ScheduleSeries series, LocalDate throughDate) {
		LocalDate target = throughDate.plusWeeks(VIEW_BUFFER_WEEKS);
		if (series.getEffectiveUntil() != null && target.isAfter(series.getEffectiveUntil())) {
			return series.getEffectiveUntil();
		}
		return target;
	}

	private void materializeRange(ScheduleSeries series, LocalDate fromDate, LocalDate toDate) {
		if (toDate.isBefore(fromDate)) {
			return;
		}

		Set<DayOfWeek> days = series.getDaysOfWeek() != null
				? ScheduleTimeSupport.fromDaysOfWeekBitmask(series.getDaysOfWeek())
				: EnumSet.noneOf(DayOfWeek.class);
		int dayOfMonth = series.getDayOfMonth() != null ? series.getDayOfMonth() : 1;

		List<LocalDate> dates = ScheduleTimeSupport.generateDates(
				series.getRecurrenceType(),
				fromDate,
				toDate,
				series.getAnchorDate(),
				days,
				dayOfMonth,
				scheduleProperties.getHorizonWeeks());

		for (LocalDate date : dates) {
			Instant startAt = ScheduleTimeSupport.toInstant(date, series.getStartTime());
			if (scheduleRepository.existsBySeriesIdAndStartAt(series.getId(), startAt)) {
				continue;
			}

			Instant endAt = ScheduleTimeSupport.toInstant(date, series.getEndTime());
			Schedule schedule = new Schedule(
					series.getChild(),
					series.getTitle(),
					series.getDescription(),
					series.getScheduleType(),
					startAt,
					endAt,
					series.getCreatedBy());
			schedule.setSeries(series);
			schedule.setAcademy(series.getAcademy());
			schedule.setSubjectCategory(series.getSubjectCategory());
			schedule.setPickupGuardian(series.getPickupGuardian());

			try {
				scheduleRepository.save(schedule);
				entityManager.flush();
			} catch (DataIntegrityViolationException exception) {
				entityManager.clear();
			}
		}
	}
}
