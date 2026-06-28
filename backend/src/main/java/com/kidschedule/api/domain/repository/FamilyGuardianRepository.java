package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.FamilyGuardian;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyGuardianRepository extends JpaRepository<FamilyGuardian, UUID> {

	Optional<FamilyGuardian> findByFamilyIdAndUserId(UUID familyId, UUID userId);

	boolean existsByFamilyIdAndUserId(UUID familyId, UUID userId);

	List<FamilyGuardian> findByUserId(UUID userId);
}
