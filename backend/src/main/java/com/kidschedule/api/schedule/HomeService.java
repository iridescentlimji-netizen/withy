package com.kidschedule.api.schedule;

import com.kidschedule.api.domain.entity.Child;
import com.kidschedule.api.domain.entity.FamilyGuardian;
import com.kidschedule.api.domain.entity.Schedule;
import com.kidschedule.api.domain.enums.ScheduleStatus;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.repository.ChildRepository;
import com.kidschedule.api.family.FamilyAccessService;
import com.kidschedule.api.web.dto.HomeResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

	private final FamilyAccessService familyAccessService;
	private final ChildRepository childRepository;
	private final ScheduleService scheduleService;

	public HomeService(
			FamilyAccessService familyAccessService,
			ChildRepository childRepository,
			ScheduleService scheduleService) {
		this.familyAccessService = familyAccessService;
		this.childRepository = childRepository;
		this.scheduleService = scheduleService;
	}

	@Transactional(readOnly = true)
	public HomeResponse getHome(UUID familyId, UUID userId) {
		FamilyGuardian guardian = familyAccessService.requireGuardian(familyId, userId);
		LocalDate today = LocalDate.now(ScheduleTimeSupport.ZONE);
		Instant now = Instant.now();
		List<Child> children = childRepository.findByFamilyIdOrderByNicknameAsc(familyId);

		List<HomeResponse.HomeChildCardResponse> childCards = new ArrayList<>();
		List<HomeResponse.HomeReturnPlanResponse> returnPlans = new ArrayList<>();

		for (Child child : children) {
			List<Schedule> todaySchedules =
					scheduleService.findActiveByFamilyAndDay(familyId, today, child.getId());
			long todayCount = todaySchedules.size();

			Schedule current = todaySchedules.stream()
					.filter(schedule -> ScheduleTimeSupport.resolveStatus(
									schedule.getStartAt(), schedule.getEndAt(), now)
							== ScheduleStatus.IN_PROGRESS)
					.findFirst()
					.orElse(null);

			Schedule next = todaySchedules.stream()
					.filter(schedule -> schedule.getStartAt().isAfter(now))
					.min(Comparator.comparing(Schedule::getStartAt))
					.orElse(null);

			childCards.add(new HomeResponse.HomeChildCardResponse(
					child.getId(),
					child.getNickname(),
					toSummary(current),
					toSummary(next),
					todayCount));

			todaySchedules.stream()
					.filter(schedule -> schedule.getScheduleType() == ScheduleType.PICKUP)
					.forEach(schedule -> returnPlans.add(new HomeResponse.HomeReturnPlanResponse(
							schedule.getId(),
							child.getId(),
							child.getNickname(),
							schedule.getStartAt(),
							schedule.getPickupGuardian() != null
									? schedule.getPickupGuardian().getId()
									: null,
							schedule.getPickupGuardian() != null
									? schedule.getPickupGuardian().getNickname()
									: null)));
		}

		return new HomeResponse(
				guardian.getUser().getNickname(),
				guardian.getRole(),
				childCards,
				returnPlans);
	}

	private HomeResponse.ScheduleSummaryResponse toSummary(Schedule schedule) {
		if (schedule == null) {
			return null;
		}
		return new HomeResponse.ScheduleSummaryResponse(
				schedule.getId(),
				schedule.getTitle(),
				schedule.getAcademy() != null ? schedule.getAcademy().getName() : null,
				schedule.getScheduleType(),
				schedule.getStartAt(),
				schedule.getEndAt());
	}
}
