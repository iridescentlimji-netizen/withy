package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.Family;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FamilyRepository extends JpaRepository<Family, UUID> {

	@Query("""
			select f from Family f
			join f.guardians g
			where g.user.id = :userId
			order by f.name asc
			""")
	List<Family> findAllByGuardianUserId(@Param("userId") UUID userId);
}
