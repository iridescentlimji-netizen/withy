package com.kidschedule.api.family;

import com.kidschedule.api.domain.entity.Child;
import com.kidschedule.api.domain.entity.Family;
import com.kidschedule.api.domain.repository.ChildRepository;
import com.kidschedule.api.web.dto.ChildResponse;
import com.kidschedule.api.web.dto.CreateChildRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChildService {

	private final ChildRepository childRepository;
	private final FamilyAccessService familyAccessService;

	public ChildService(ChildRepository childRepository, FamilyAccessService familyAccessService) {
		this.childRepository = childRepository;
		this.familyAccessService = familyAccessService;
	}

	@Transactional(readOnly = true)
	public List<ChildResponse> listChildren(UUID familyId, UUID userId) {
		familyAccessService.requireFamilyAccess(familyId, userId);
		return childRepository.findByFamilyIdOrderByNicknameAsc(familyId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public ChildResponse createChild(UUID familyId, UUID userId, CreateChildRequest request) {
		Family family = familyAccessService.requireCanEdit(familyId, userId).getFamily();
		Child child = childRepository.save(new Child(family, request.nickname(), request.birthYear()));
		return toResponse(child);
	}

	private ChildResponse toResponse(Child child) {
		return new ChildResponse(child.getId(), child.getNickname(), child.getBirthYear());
	}
}
