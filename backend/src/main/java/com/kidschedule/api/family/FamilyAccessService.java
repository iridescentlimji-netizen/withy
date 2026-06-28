package com.kidschedule.api.family;

import com.kidschedule.api.domain.entity.Family;
import com.kidschedule.api.domain.entity.FamilyGuardian;
import com.kidschedule.api.domain.repository.FamilyGuardianRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FamilyAccessService {

	private final FamilyGuardianRepository familyGuardianRepository;

	public FamilyAccessService(FamilyGuardianRepository familyGuardianRepository) {
		this.familyGuardianRepository = familyGuardianRepository;
	}

	public FamilyGuardian requireGuardian(UUID familyId, UUID userId) {
		return familyGuardianRepository
				.findByFamilyIdAndUserId(familyId, userId)
				.orElseThrow(() -> new IllegalArgumentException("You are not a member of this family"));
	}

	public Family requireFamilyAccess(UUID familyId, UUID userId) {
		return requireGuardian(familyId, userId).getFamily();
	}

	public FamilyGuardian requireCanEdit(UUID familyId, UUID userId) {
		FamilyGuardian guardian = requireGuardian(familyId, userId);
		if (!guardian.isCanEdit()) {
			throw new IllegalArgumentException("You do not have permission to edit this family");
		}
		return guardian;
	}
}
