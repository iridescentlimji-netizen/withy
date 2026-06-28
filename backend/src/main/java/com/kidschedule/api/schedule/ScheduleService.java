package com.kidschedule.api.schedule;

import com.kidschedule.api.config.ScheduleProperties;
import com.kidschedule.api.domain.entity.Academy;
import com.kidschedule.api.domain.entity.Child;
import com.kidschedule.api.domain.entity.Schedule;
import com.kidschedule.api.domain.entity.ScheduleSeries;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.enums.CancelScope;
import com.kidschedule.api.domain.enums.RecurrenceType;
import com.kidschedule.api.domain.enums.ScheduleStatus;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.repository.ChildRepository;
import com.kidschedule.api.domain.repository.ScheduleRepository;
import com.kidschedule.api.domain.repository.ScheduleSeriesRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.family.AcademyService;
import com.kidschedule.api.family.FamilyAccessService;
import com.kidschedule.api.web.dto.CancelScheduleRequest;
import com.kidschedule.api.web.dto.CreateScheduleRequest;
import com.kidschedule.api.web.dto.ScheduleCalendarResponse;
import com.kidschedule.api.web.dto.ScheduleDayListResponse;
import com.kidschedule.api.web.dto.ScheduleResponse;
import com.kidschedule.api.web.dto.UpdateScheduleRequest;
import com.kidschedule.api.web.dto.UpdateScheduleSeriesRequest;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final ScheduleSeriesRepository scheduleSeriesRepository;
	private final ChildRepository childRepository;
	private final UserRepository userRepository;
	private final FamilyAccessService familyAccessService;
	private final AcademyService academyService;
	private final ScheduleProperties scheduleProperties;
	private final ScheduleSeriesMaterializer scheduleSeriesMaterializer;

	public ScheduleService(
			ScheduleRepository scheduleRepository,
			ScheduleSeriesRepository scheduleSeriesRepository,
			ChildRepository childRepository,
			UserRepository userRepository,
			FamilyAccessService familyAccessService,
			AcademyService academyService,
			ScheduleProperties scheduleProperties,
			ScheduleSeriesMaterializer scheduleSeriesMaterializer) {
		this.scheduleRepository = scheduleRepository;
		this.scheduleSeriesRepository = scheduleSeriesRepository;
		this.childRepository = childRepository;
		this.userRepository = userRepository;
		this.familyAccessService = familyAccessService;
		this.academyService = academyService;
		this.scheduleProperties = scheduleProperties;
		this.scheduleSeriesMaterializer = scheduleSeriesMaterializer;
	}

	@Transactional
	public ScheduleResponse createSchedule(UUID familyId, UUID userId, CreateScheduleRequest request) {
		familyAccessService.requireCanEdit(familyId, userId);
		User creator = userRepository
				.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		Child child = requireChild(familyId, request.childId());
		Academy academy = resolveAcademy(familyId, request.academyId());
		User pickupGuardian = resolvePickupGuardian(familyId, request.pickupGuardianId());

		if (request.recurrence() == RecurrenceType.NONE) {
			Schedule schedule = createOneTimeSchedule(request, child, academy, pickupGuardian, creator);
			return toResponse(schedule, Instant.now());
		}

		ScheduleSeries series = createSeries(request, child, academy, pickupGuardian, creator);
		scheduleSeriesMaterializer.materializeInitial(series);
		Schedule first = scheduleRepository.findBySeriesIdAndStartAtGreaterThanEqualAndCancelledFalse(
						series.getId(), Instant.EPOCH)
				.stream()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Failed to generate schedule occurrences"));
		return toResponse(first, Instant.now());
	}

	@Transactional
	public ScheduleDayListResponse listSchedulesForDay(
			UUID familyId, UUID userId, LocalDate date, UUID childId) {
		familyAccessService.requireFamilyAccess(familyId, userId);
		scheduleSeriesMaterializer.ensureMaterializedThrough(familyId, date);
		Instant rangeStart = ScheduleTimeSupport.toInstant(date, LocalTime.MIN);
		Instant rangeEnd = ScheduleTimeSupport.toInstant(date.plusDays(1), LocalTime.MIN);
		List<Schedule> schedules = scheduleRepository.findActiveByFamilyAndRange(
				familyId, rangeStart, rangeEnd, childId);
		Instant now = Instant.now();

		List<ScheduleResponse> upcoming = new ArrayList<>();
		List<ScheduleResponse> inProgress = new ArrayList<>();
		List<ScheduleResponse> completed = new ArrayList<>();

		for (Schedule schedule : schedules) {
			ScheduleResponse response = toResponse(schedule, now);
			switch (response.status()) {
				case UPCOMING -> upcoming.add(response);
				case IN_PROGRESS -> inProgress.add(response);
				case COMPLETED -> completed.add(response);
			}
		}

		return new ScheduleDayListResponse(upcoming, inProgress, completed);
	}

	@Transactional
	public ScheduleCalendarResponse getCalendarCounts(
			UUID familyId, UUID userId, LocalDate month, UUID childId) {
		familyAccessService.requireFamilyAccess(familyId, userId);
		LocalDate start = month.withDayOfMonth(1);
		LocalDate end = start.plusMonths(1).minusDays(1);
		scheduleSeriesMaterializer.ensureMaterializedThrough(familyId, end);
		LocalDate rangeEndDate = start.plusMonths(1);
		Instant rangeStart = ScheduleTimeSupport.toInstant(start, LocalTime.MIN);
		Instant rangeEnd = ScheduleTimeSupport.toInstant(rangeEndDate, LocalTime.MIN);

		List<Schedule> schedules = scheduleRepository.findActiveByFamilyAndRange(
				familyId, rangeStart, rangeEnd, childId);
		Map<LocalDate, Long> counts = schedules.stream()
				.collect(Collectors.groupingBy(
						schedule -> LocalDate.ofInstant(schedule.getStartAt(), ScheduleTimeSupport.ZONE),
						Collectors.counting()));

		return new ScheduleCalendarResponse(new HashMap<>(counts));
	}

	@Transactional(readOnly = true)
	public ScheduleResponse getSchedule(UUID familyId, UUID scheduleId, UUID userId) {
		familyAccessService.requireFamilyAccess(familyId, userId);
		Schedule schedule = requireSchedule(familyId, scheduleId);
		return toResponse(schedule, Instant.now());
	}

	@Transactional
	public ScheduleResponse updateSchedule(
			UUID familyId, UUID scheduleId, UUID userId, UpdateScheduleRequest request) {
		familyAccessService.requireCanEdit(familyId, userId);
		Schedule schedule = requireSchedule(familyId, scheduleId);
		if (schedule.isCancelled()) {
			throw new IllegalArgumentException("Cancelled schedules cannot be updated");
		}
		if (!request.endAt().isAfter(request.startAt())) {
			throw new IllegalArgumentException("endAt must be after startAt");
		}

		Academy academy = resolveAcademy(familyId, request.academyId());
		User pickupGuardian = resolvePickupGuardian(familyId, request.pickupGuardianId());

		schedule.setTitle(request.title());
		schedule.setDescription(request.description());
		schedule.setScheduleType(request.scheduleType());
		schedule.setStartAt(request.startAt());
		schedule.setEndAt(request.endAt());
		applyScheduleDetails(schedule, academy, request.subjectCategory(), pickupGuardian);
		return toResponse(schedule, Instant.now());
	}

	@Transactional
	public void cancelSchedule(
			UUID familyId, UUID scheduleId, UUID userId, CancelScheduleRequest request) {
		familyAccessService.requireCanEdit(familyId, userId);
		Schedule schedule = requireSchedule(familyId, scheduleId);
		CancelScope scope = request != null ? request.scope() : CancelScope.OCCURRENCE;

		if (scope == CancelScope.FUTURE && schedule.getSeries() != null) {
			ScheduleSeries series = schedule.getSeries();
			LocalDate cancelFromDate =
					LocalDate.ofInstant(schedule.getStartAt(), ScheduleTimeSupport.ZONE);
			LocalDate newEffectiveUntil = cancelFromDate.minusDays(1);

			scheduleRepository.cancelActiveBySeriesIdFrom(series.getId(), schedule.getStartAt());

			if (series.getEffectiveUntil() == null || newEffectiveUntil.isBefore(series.getEffectiveUntil())) {
				series.setEffectiveUntil(newEffectiveUntil);
			}
			return;
		}

		schedule.setCancelled(true);
	}

	@Transactional
	public ScheduleResponse updateSeries(
			UUID familyId, UUID seriesId, UUID userId, UpdateScheduleSeriesRequest request) {
		familyAccessService.requireCanEdit(familyId, userId);
		ScheduleSeries series = scheduleSeriesRepository
				.findById(seriesId)
				.orElseThrow(() -> new IllegalArgumentException("Schedule series not found"));
		if (!series.getChild().getFamily().getId().equals(familyId)) {
			throw new IllegalArgumentException("Schedule series not found");
		}
		if (!request.endTime().isAfter(request.startTime())) {
			throw new IllegalArgumentException("endTime must be after startTime");
		}

		Academy academy = resolveAcademy(familyId, request.academyId());
		User pickupGuardian = resolvePickupGuardian(familyId, request.pickupGuardianId());

		series.setTitle(request.title());
		series.setDescription(request.description());
		series.setScheduleType(request.scheduleType());
		series.setStartTime(request.startTime());
		series.setEndTime(request.endTime());
		series.setAcademy(academy);
		series.setSubjectCategory(resolveSubjectCategory(request.subjectCategory(), academy));
		series.setPickupGuardian(pickupGuardian);

		LocalDate today = LocalDate.now(ScheduleTimeSupport.ZONE);
		for (Schedule future : scheduleRepository.findActiveBySeriesIdFrom(
				series.getId(), ScheduleTimeSupport.toInstant(today, LocalTime.MIN))) {
			future.setCancelled(true);
		}

		scheduleSeriesMaterializer.ensureMaterializedThrough(familyId, today.plusWeeks(scheduleProperties.getHorizonWeeks()));

		Schedule next = scheduleRepository.findBySeriesIdAndStartAtGreaterThanEqualAndCancelledFalse(
						series.getId(), ScheduleTimeSupport.toInstant(today, LocalTime.MIN))
				.stream()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Failed to regenerate schedule occurrences"));
		return toResponse(next, Instant.now());
	}

	@Transactional(readOnly = true)
	public List<Schedule> findActiveByFamilyAndDay(UUID familyId, LocalDate date, UUID childId) {
		Instant rangeStart = ScheduleTimeSupport.toInstant(date, LocalTime.MIN);
		Instant rangeEnd = ScheduleTimeSupport.toInstant(date.plusDays(1), LocalTime.MIN);
		return scheduleRepository.findActiveByFamilyAndRange(familyId, rangeStart, rangeEnd, childId);
	}

	private Schedule createOneTimeSchedule(
			CreateScheduleRequest request,
			Child child,
			Academy academy,
			User pickupGuardian,
			User creator) {
		if (request.startAt() == null || request.endAt() == null) {
			throw new IllegalArgumentException("startAt and endAt are required for one-time schedules");
		}
		if (!request.endAt().isAfter(request.startAt())) {
			throw new IllegalArgumentException("endAt must be after startAt");
		}

		Schedule schedule = new Schedule(
				child,
				request.title(),
				request.description(),
				request.scheduleType(),
				request.startAt(),
				request.endAt(),
				creator);
		applyScheduleDetails(schedule, academy, request.subjectCategory(), pickupGuardian);
		return scheduleRepository.save(schedule);
	}

	private ScheduleSeries createSeries(
			CreateScheduleRequest request,
			Child child,
			Academy academy,
			User pickupGuardian,
			User creator) {
		validateRecurringRequest(request);

		ScheduleSeries series = ScheduleSeries.builder()
				.child(child)
				.academy(academy)
				.title(request.title())
				.description(request.description())
				.scheduleType(request.scheduleType())
				.subjectCategory(resolveSubjectCategory(request, academy))
				.pickupGuardian(pickupGuardian)
				.recurrenceType(request.recurrence())
				.daysOfWeek(toBitmask(request.daysOfWeek()))
				.dayOfMonth(request.dayOfMonth() != null ? request.dayOfMonth().shortValue() : null)
				.anchorDate(request.anchorDate() != null ? request.anchorDate() : request.effectiveFrom())
				.startTime(request.startTime())
				.endTime(request.endTime())
				.effectiveFrom(request.effectiveFrom())
				.effectiveUntil(request.effectiveUntil())
				.createdBy(creator)
				.build();

		return scheduleSeriesRepository.save(series);
	}

	private void validateRecurringRequest(CreateScheduleRequest request) {
		if (request.startTime() == null || request.endTime() == null || request.effectiveFrom() == null) {
			throw new IllegalArgumentException("startTime, endTime, and effectiveFrom are required for recurring schedules");
		}
		if (!request.endTime().isAfter(request.startTime())) {
			throw new IllegalArgumentException("endTime must be after startTime");
		}
		if (request.recurrence() == RecurrenceType.MONTHLY) {
			if (request.dayOfMonth() == null || request.dayOfMonth() < 1 || request.dayOfMonth() > 31) {
				throw new IllegalArgumentException("dayOfMonth must be between 1 and 31 for monthly schedules");
			}
		} else if (request.daysOfWeek() == null || request.daysOfWeek().isEmpty()) {
			throw new IllegalArgumentException("daysOfWeek are required for weekly schedules");
		}
	}

	private Child requireChild(UUID familyId, UUID childId) {
		Child child = childRepository
				.findById(childId)
				.orElseThrow(() -> new IllegalArgumentException("Child not found"));
		if (!child.getFamily().getId().equals(familyId)) {
			throw new IllegalArgumentException("Child not found");
		}
		return child;
	}

	private Schedule requireSchedule(UUID familyId, UUID scheduleId) {
		Schedule schedule = scheduleRepository
				.findDetailedById(scheduleId)
				.orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
		if (!schedule.getChild().getFamily().getId().equals(familyId)) {
			throw new IllegalArgumentException("Schedule not found");
		}
		return schedule;
	}

	private Academy resolveAcademy(UUID familyId, UUID academyId) {
		if (academyId == null) {
			return null;
		}
		return academyService.requireAcademy(familyId, academyId);
	}

	private User resolvePickupGuardian(UUID familyId, UUID pickupGuardianId) {
		if (pickupGuardianId == null) {
			return null;
		}
		familyAccessService.requireGuardian(familyId, pickupGuardianId);
		return userRepository
				.findById(pickupGuardianId)
				.orElseThrow(() -> new IllegalArgumentException("Pickup guardian not found"));
	}

	private void applyScheduleDetails(
			Schedule schedule, Academy academy, com.kidschedule.api.domain.enums.SubjectCategory subjectCategory, User pickupGuardian) {
		schedule.setAcademy(academy);
		schedule.setSubjectCategory(resolveSubjectCategory(subjectCategory, academy));
		schedule.setPickupGuardian(pickupGuardian);
	}

	private com.kidschedule.api.domain.enums.SubjectCategory resolveSubjectCategory(
			CreateScheduleRequest request, Academy academy) {
		if (request.subjectCategory() != null) {
			return request.subjectCategory();
		}
		return academy != null ? academy.getDefaultSubjectCategory() : null;
	}

	private com.kidschedule.api.domain.enums.SubjectCategory resolveSubjectCategory(
			com.kidschedule.api.domain.enums.SubjectCategory subjectCategory, Academy academy) {
		if (subjectCategory != null) {
			return subjectCategory;
		}
		return academy != null ? academy.getDefaultSubjectCategory() : null;
	}

	private Short toBitmask(List<DayOfWeek> daysOfWeek) {
		if (daysOfWeek == null || daysOfWeek.isEmpty()) {
			return null;
		}
		return ScheduleTimeSupport.toDaysOfWeekBitmask(EnumSet.copyOf(daysOfWeek));
	}

	static ScheduleResponse toResponse(Schedule schedule, Instant now) {
		Child child = schedule.getChild();
		Academy academy = schedule.getAcademy();
		User pickupGuardian = schedule.getPickupGuardian();
		ScheduleSeries series = schedule.getSeries();
		return new ScheduleResponse(
				schedule.getId(),
				series != null ? series.getId() : null,
				child.getId(),
				child.getNickname(),
				academy != null ? academy.getId() : null,
				academy != null ? academy.getName() : null,
				schedule.getTitle(),
				schedule.getDescription(),
				schedule.getScheduleType(),
				schedule.getSubjectCategory(),
				pickupGuardian != null ? pickupGuardian.getId() : null,
				pickupGuardian != null ? pickupGuardian.getNickname() : null,
				schedule.getStartAt(),
				schedule.getEndAt(),
				ScheduleTimeSupport.resolveStatus(schedule.getStartAt(), schedule.getEndAt(), now),
				schedule.isCancelled());
	}
}
