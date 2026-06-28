package com.kidschedule.api.family;

import com.kidschedule.api.domain.entity.Family;
import com.kidschedule.api.domain.entity.FamilyGuardian;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.enums.MemberRole;
import com.kidschedule.api.domain.repository.FamilyGuardianRepository;
import com.kidschedule.api.domain.repository.FamilyRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.web.dto.CreateFamilyRequest;
import com.kidschedule.api.web.dto.FamilyResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyService {

	private final FamilyRepository familyRepository;
	private final FamilyGuardianRepository familyGuardianRepository;
	private final UserRepository userRepository;
	private final FamilyAccessService familyAccessService;

	public FamilyService(
			FamilyRepository familyRepository,
			FamilyGuardianRepository familyGuardianRepository,
			UserRepository userRepository,
			FamilyAccessService familyAccessService) {
		this.familyRepository = familyRepository;
		this.familyGuardianRepository = familyGuardianRepository;
		this.userRepository = userRepository;
		this.familyAccessService = familyAccessService;
	}

	@Transactional(readOnly = true)
	public List<FamilyResponse> listMyFamilies(UUID userId) {
		return familyRepository.findAllByGuardianUserId(userId).stream()
				.map(family -> toResponse(family, userId))
				.toList();
	}

	@Transactional(readOnly = true)
	public FamilyResponse getFamily(UUID familyId, UUID userId) {
		Family family = familyAccessService.requireFamilyAccess(familyId, userId);
		return toResponse(family, userId);
	}

	@Transactional
	public FamilyResponse createFamily(UUID userId, CreateFamilyRequest request) {
		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Family family = familyRepository.save(new Family(request.name(), user));
		FamilyGuardian guardian =
				familyGuardianRepository.save(new FamilyGuardian(family, user, MemberRole.MASTER, true));
		return new FamilyResponse(family.getId(), family.getName(), guardian.getRole(), guardian.isCanEdit());
	}

	private FamilyResponse toResponse(Family family, UUID userId) {
		FamilyGuardian guardian = familyAccessService.requireGuardian(family.getId(), userId);
		return new FamilyResponse(family.getId(), family.getName(), guardian.getRole(), guardian.isCanEdit());
	}
}
