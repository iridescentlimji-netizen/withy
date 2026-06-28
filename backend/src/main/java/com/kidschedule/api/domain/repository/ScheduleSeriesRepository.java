package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.ScheduleSeries;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleSeriesRepository extends JpaRepository<ScheduleSeries, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ss from ScheduleSeries ss where ss.id = :id")
	Optional<ScheduleSeries> findByIdForUpdate(@Param("id") UUID id);

	@Query("""
			select ss from ScheduleSeries ss
			join ss.child c
			where c.family.id = :familyId
			""")
	List<ScheduleSeries> findByFamilyId(@Param("familyId") UUID familyId);
}
