package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.Academy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademyRepository extends JpaRepository<Academy, UUID> {

	List<Academy> findByFamilyIdOrderByNameAsc(UUID familyId);

	List<Academy> findByFamilyIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID familyId, String name);

	@Query("""
			select a from Academy a
			where a.family.id = :familyId
			  and (
			    lower(a.name) like lower(concat('%', :query, '%'))
			    or lower(replace(a.name, ' ', '')) like lower(concat('%', replace(:query, ' ', ''), '%'))
			  )
			order by a.name asc
			""")
	List<Academy> searchByFamilyIdAndName(@Param("familyId") UUID familyId, @Param("query") String query);

	@Query("""
			select a from Academy a
			where a.family.id = :familyId
			  and lower(replace(a.name, ' ', '')) = lower(replace(:name, ' ', ''))
			""")
	Optional<Academy> findByFamilyIdAndNormalizedName(@Param("familyId") UUID familyId, @Param("name") String name);
}
