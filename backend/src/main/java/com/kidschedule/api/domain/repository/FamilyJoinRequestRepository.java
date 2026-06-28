package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.FamilyJoinRequest;
import com.kidschedule.api.domain.enums.JoinStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyJoinRequestRepository extends JpaRepository<FamilyJoinRequest, UUID> {

	Optional<FamilyJoinRequest> findByFamilyIdAndUserIdAndStatus(
			UUID familyId, UUID userId, JoinStatus status);

	List<FamilyJoinRequest> findByFamilyIdAndStatusOrderByCreatedAtDesc(UUID familyId, JoinStatus status);
}
