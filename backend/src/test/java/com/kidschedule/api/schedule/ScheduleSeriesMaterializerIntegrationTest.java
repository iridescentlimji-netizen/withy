package com.kidschedule.api.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import com.kidschedule.api.domain.entity.Child;
import com.kidschedule.api.domain.entity.Family;
import com.kidschedule.api.domain.entity.FamilyGuardian;
import com.kidschedule.api.domain.entity.ScheduleSeries;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.enums.AccountType;
import com.kidschedule.api.domain.enums.MemberRole;
import com.kidschedule.api.domain.enums.RecurrenceType;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.repository.ChildRepository;
import com.kidschedule.api.domain.repository.FamilyGuardianRepository;
import com.kidschedule.api.domain.repository.FamilyRepository;
import com.kidschedule.api.domain.repository.ScheduleRepository;
import com.kidschedule.api.domain.repository.ScheduleSeriesRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleSeriesMaterializerIntegrationTest {

	@Autowired
	private ScheduleSeriesMaterializer materializer;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private ScheduleSeriesRepository scheduleSeriesRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyRepository familyRepository;

	@Autowired
	private FamilyGuardianRepository familyGuardianRepository;

	@Autowired
	private ChildRepository childRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	private UUID familyId;
	private UUID seriesId;

	@BeforeEach
	void setUp() {
		transactionTemplate.executeWithoutResult(status -> {
			User user = userRepository.save(new User("materializer-test", AccountType.ADULT));
			Family family = familyRepository.save(new Family("materializer family", user));
			familyGuardianRepository.save(new FamilyGuardian(family, user, MemberRole.MASTER, true));
			Child child = childRepository.save(new Child(family, "테스트아이", (short) 2018));

			ScheduleSeries series = ScheduleSeries.builder()
					.child(child)
					.title("테스트 반복")
					.scheduleType(ScheduleType.ACTIVITY)
					.recurrenceType(RecurrenceType.WEEKLY)
					.daysOfWeek(ScheduleTimeSupport.toDaysOfWeekBitmask(EnumSet.of(DayOfWeek.TUESDAY)))
					.startTime(LocalTime.of(15, 0))
					.endTime(LocalTime.of(16, 0))
					.effectiveFrom(LocalDate.of(2026, 6, 28))
					.anchorDate(LocalDate.of(2026, 6, 28))
					.createdBy(user)
					.build();

			series = scheduleSeriesRepository.save(series);
			familyId = family.getId();
			seriesId = series.getId();
		});
	}

	@Test
	void materializingTwiceDoesNotCreateDuplicates() {
		transactionTemplate.executeWithoutResult(status -> materializer.materializeInitial(
				scheduleSeriesRepository.findById(seriesId).orElseThrow()));
		long afterInitial = countActiveOccurrences();

		transactionTemplate.executeWithoutResult(
				status -> materializer.ensureMaterializedThrough(familyId, LocalDate.of(2026, 10, 1)));
		long afterExtension = countActiveOccurrences();

		transactionTemplate.executeWithoutResult(
				status -> materializer.ensureMaterializedThrough(familyId, LocalDate.of(2026, 10, 1)));
		long afterSecondExtension = countActiveOccurrences();

		assertThat(afterInitial).isGreaterThan(0);
		assertThat(afterExtension).isGreaterThanOrEqualTo(afterInitial);
		assertThat(afterSecondExtension).isEqualTo(afterExtension);
	}

	private long countActiveOccurrences() {
		return scheduleRepository
				.findBySeriesIdAndStartAtGreaterThanEqualAndCancelledFalse(seriesId, Instant.EPOCH)
				.size();
	}
}
