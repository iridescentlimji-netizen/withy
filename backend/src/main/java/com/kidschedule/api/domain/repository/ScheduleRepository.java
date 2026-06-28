package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

	@Query("""
			select s from Schedule s
			join fetch s.child c
			left join fetch s.academy a
			left join fetch s.pickupGuardian pg
			where c.family.id = :familyId
			  and s.cancelled = false
			  and s.startAt < :rangeEnd
			  and s.endAt > :rangeStart
			  and (:childId is null or c.id = :childId)
			order by s.startAt asc
			""")
	List<Schedule> findActiveByFamilyAndRange(
			@Param("familyId") UUID familyId,
			@Param("rangeStart") Instant rangeStart,
			@Param("rangeEnd") Instant rangeEnd,
			@Param("childId") UUID childId);

	@Query("""
			select cast(s.startAt as date), count(s)
			from Schedule s
			join s.child c
			where c.family.id = :familyId
			  and s.cancelled = false
			  and s.startAt >= :rangeStart
			  and s.startAt < :rangeEnd
			  and (:childId is null or c.id = :childId)
			group by cast(s.startAt as date)
			""")
	List<Object[]> countByDay(
			@Param("familyId") UUID familyId,
			@Param("rangeStart") Instant rangeStart,
			@Param("rangeEnd") Instant rangeEnd,
			@Param("childId") UUID childId);

	List<Schedule> findBySeriesIdAndStartAtGreaterThanEqualAndCancelledFalse(UUID seriesId, Instant startAt);

	@Query("""
			select s from Schedule s
			join fetch s.child c
			left join fetch s.academy a
			left join fetch s.pickupGuardian pg
			left join fetch s.series sr
			where s.id = :scheduleId
			""")
	Optional<Schedule> findDetailedById(@Param("scheduleId") UUID scheduleId);

	@Query("""
			select max(s.startAt) from Schedule s
			where s.series.id = :seriesId
			  and s.cancelled = false
			""")
	Optional<Instant> findLatestStartAtBySeriesId(@Param("seriesId") UUID seriesId);

	boolean existsBySeriesIdAndStartAtAndCancelledFalse(UUID seriesId, Instant startAt);

	boolean existsBySeriesIdAndStartAt(UUID seriesId, Instant startAt);

	@Query("""
			select s from Schedule s
			where s.series.id = :seriesId
			  and s.cancelled = false
			  and s.startAt >= :fromInstant
			""")
	List<Schedule> findActiveBySeriesIdFrom(
			@Param("seriesId") UUID seriesId, @Param("fromInstant") Instant fromInstant);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update Schedule s
			set s.cancelled = true
			where s.series.id = :seriesId
			  and s.cancelled = false
			  and s.startAt >= :fromInstant
			""")
	int cancelActiveBySeriesIdFrom(
			@Param("seriesId") UUID seriesId, @Param("fromInstant") Instant fromInstant);

	@Modifying
	void deleteBySeriesIdAndStartAtGreaterThanEqualAndCancelledFalse(UUID seriesId, Instant startAt);
}
