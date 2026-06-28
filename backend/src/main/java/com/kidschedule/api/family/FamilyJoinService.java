package com.kidschedule.api.family;

import com.kidschedule.api.domain.entity.Family;
import com.kidschedule.api.domain.entity.FamilyGuardian;
import com.kidschedule.api.domain.entity.FamilyJoinRequest;
import com.kidschedule.api.domain.entity.User;
import com.kidschedule.api.domain.enums.JoinStatus;
import com.kidschedule.api.domain.enums.MemberRole;
import com.kidschedule.api.domain.repository.FamilyGuardianRepository;
import com.kidschedule.api.domain.repository.FamilyJoinRequestRepository;
import com.kidschedule.api.domain.repository.FamilyRepository;
import com.kidschedule.api.domain.repository.UserRepository;
import com.kidschedule.api.family.invite.InviteCodePayload;
import com.kidschedule.api.family.invite.InviteCodeStore;
import com.kidschedule.api.web.dto.FamilyJoinRequestResponse;
import com.kidschedule.api.web.dto.JoinFamilyRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyJoinService {

	private final InviteCodeStore inviteCodeStore;
	private final FamilyRepository familyRepository;
	private final FamilyGuardianRepository familyGuardianRepository;
	private final FamilyJoinRequestRepository familyJoinRequestRepository;
	private final UserRepository userRepository;
	private final FamilyAccessService familyAccessService;

	public FamilyJoinService(
			InviteCodeStore inviteCodeStore,
			FamilyRepository familyRepository,
			FamilyGuardianRepository familyGuardianRepository,
			FamilyJoinRequestRepository familyJoinRequestRepository,
			UserRepository userRepository,
			FamilyAccessService familyAccessService) {
		this.inviteCodeStore = inviteCodeStore;
		this.familyRepository = familyRepository;
		this.familyGuardianRepository = familyGuardianRepository;
		this.familyJoinRequestRepository = familyJoinRequestRepository;
		this.userRepository = userRepository;
		this.familyAccessService = familyAccessService;
	}

	@Transactional
	public FamilyJoinRequestResponse submitJoinRequest(UUID userId, JoinFamilyRequest request) {
		if (!familyGuardianRepository.findByUserId(userId).isEmpty()) {
			throw new IllegalArgumentException("You already belong to a family");
		}

		String normalizedCode = request.code().trim().toUpperCase();
		InviteCodePayload payload = inviteCodeStore
				.find(normalizedCode)
				.orElseThrow(() -> new IllegalArgumentException("Invalid or expired invite code"));

		familyRepository
				.findById(payload.familyId())
				.orElseThrow(() -> new IllegalArgumentException("Family not found"));

		familyJoinRequestRepository
				.findByFamilyIdAndUserIdAndStatus(payload.familyId(), userId, JoinStatus.PENDING)
				.ifPresent(existing -> {
					throw new IllegalArgumentException("You already have a pending join request");
				});

		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		FamilyJoinRequest joinRequest = familyJoinRequestRepository.save(new FamilyJoinRequest(
				familyRepository.getReferenceById(payload.familyId()),
				user,
				payload.role(),
				payload.canEdit(),
				hashCode(normalizedCode)));

		return toResponse(joinRequest);
	}

	@Transactional(readOnly = true)
	public List<FamilyJoinRequestResponse> listPendingRequests(UUID familyId, UUID userId) {
		requireMaster(familyId, userId);
		return familyJoinRequestRepository.findByFamilyIdAndStatusOrderByCreatedAtDesc(familyId, JoinStatus.PENDING)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public FamilyJoinRequestResponse approveJoinRequest(UUID familyId, UUID requestId, UUID userId) {
		requireMaster(familyId, userId);
		FamilyJoinRequest joinRequest = requirePendingRequest(familyId, requestId);

		if (!familyGuardianRepository.findByUserId(joinRequest.getUser().getId()).isEmpty()) {
			joinRequest.reject(userRepository.getReferenceById(userId));
			throw new IllegalArgumentException("User already belongs to another family");
		}

		FamilyGuardian guardian = new FamilyGuardian(
				joinRequest.getFamily(), joinRequest.getUser(), joinRequest.getRole(), joinRequest.isCanEdit());
		familyGuardianRepository.save(guardian);
		joinRequest.approve(userRepository.getReferenceById(userId));

		return toResponse(joinRequest);
	}

	@Transactional
	public FamilyJoinRequestResponse rejectJoinRequest(UUID familyId, UUID requestId, UUID userId) {
		requireMaster(familyId, userId);
		FamilyJoinRequest joinRequest = requirePendingRequest(familyId, requestId);
		joinRequest.reject(userRepository.getReferenceById(userId));
		return toResponse(joinRequest);
	}

	private FamilyJoinRequest requirePendingRequest(UUID familyId, UUID requestId) {
		FamilyJoinRequest joinRequest = familyJoinRequestRepository
				.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Join request not found"));
		if (!joinRequest.getFamily().getId().equals(familyId)) {
			throw new IllegalArgumentException("Join request not found");
		}
		if (joinRequest.getStatus() != JoinStatus.PENDING) {
			throw new IllegalArgumentException("Join request is not pending");
		}
		return joinRequest;
	}

	private void requireMaster(UUID familyId, UUID userId) {
		var guardian = familyAccessService.requireGuardian(familyId, userId);
		if (guardian.getRole() != MemberRole.MASTER) {
			throw new IllegalArgumentException("Only the family master can review join requests");
		}
	}

	private FamilyJoinRequestResponse toResponse(FamilyJoinRequest joinRequest) {
		Family family = joinRequest.getFamily();
		User user = joinRequest.getUser();
		return new FamilyJoinRequestResponse(
				joinRequest.getId(),
				family.getId(),
				family.getName(),
				user.getId(),
				user.getNickname(),
				joinRequest.getRole(),
				joinRequest.isCanEdit(),
				joinRequest.getStatus());
	}

	private String hashCode(String code) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(code.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 not available", exception);
		}
	}
}
