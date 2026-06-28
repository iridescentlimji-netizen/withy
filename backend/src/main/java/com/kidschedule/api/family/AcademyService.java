package com.kidschedule.api.family;

import com.kidschedule.api.domain.entity.Academy;
import com.kidschedule.api.domain.entity.Family;
import com.kidschedule.api.domain.repository.AcademyRepository;
import com.kidschedule.api.web.dto.AcademyResponse;
import com.kidschedule.api.web.dto.CreateAcademyRequest;
import com.kidschedule.api.web.dto.UpdateAcademyRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AcademyService {

	private final AcademyRepository academyRepository;
	private final FamilyAccessService familyAccessService;

	public AcademyService(AcademyRepository academyRepository, FamilyAccessService familyAccessService) {
		this.academyRepository = academyRepository;
		this.familyAccessService = familyAccessService;
	}

	@Transactional(readOnly = true)
	public List<AcademyResponse> listAcademies(UUID familyId, UUID userId, String query) {
		familyAccessService.requireFamilyAccess(familyId, userId);
		List<Academy> academies = StringUtils.hasText(query)
				? academyRepository.searchByFamilyIdAndName(familyId, query.trim())
				: academyRepository.findByFamilyIdOrderByNameAsc(familyId);
		return academies.stream().map(this::toResponse).toList();
	}

	@Transactional
	public AcademyResponse createAcademy(UUID familyId, UUID userId, CreateAcademyRequest request) {
		Family family = familyAccessService.requireCanEdit(familyId, userId).getFamily();
		academyRepository
				.findByFamilyIdAndNormalizedName(familyId, request.name())
				.ifPresent(existing -> {
					throw new IllegalArgumentException(
							"이미 등록된 학원입니다: " + existing.getName());
				});
		Academy academy = academyRepository.save(new Academy(
				family,
				request.name(),
				request.phone(),
				request.defaultSubjectCategory(),
				request.memo()));
		return toResponse(academy);
	}

	@Transactional
	public AcademyResponse updateAcademy(UUID familyId, UUID academyId, UUID userId, UpdateAcademyRequest request) {
		familyAccessService.requireCanEdit(familyId, userId);
		Academy academy = requireAcademy(familyId, academyId);
		if (StringUtils.hasText(request.name())) {
			academy.setName(request.name());
		}
		if (request.phone() != null) {
			academy.setPhone(request.phone());
		}
		if (request.defaultSubjectCategory() != null) {
			academy.setDefaultSubjectCategory(request.defaultSubjectCategory());
		}
		if (request.memo() != null) {
			academy.setMemo(request.memo());
		}
		return toResponse(academy);
	}

	@Transactional
	public void deleteAcademy(UUID familyId, UUID academyId, UUID userId) {
		familyAccessService.requireCanEdit(familyId, userId);
		Academy academy = requireAcademy(familyId, academyId);
		academyRepository.delete(academy);
	}

	public Academy requireAcademy(UUID familyId, UUID academyId) {
		Academy academy = academyRepository
				.findById(academyId)
				.orElseThrow(() -> new IllegalArgumentException("Academy not found"));
		if (!academy.getFamily().getId().equals(familyId)) {
			throw new IllegalArgumentException("Academy not found");
		}
		return academy;
	}

	private AcademyResponse toResponse(Academy academy) {
		return new AcademyResponse(
				academy.getId(),
				academy.getName(),
				academy.getPhone(),
				academy.getDefaultSubjectCategory(),
				academy.getMemo());
	}
}
