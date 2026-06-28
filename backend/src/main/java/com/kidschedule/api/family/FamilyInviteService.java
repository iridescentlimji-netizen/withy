package com.kidschedule.api.family;

import com.kidschedule.api.domain.enums.MemberRole;
import com.kidschedule.api.family.invite.InviteCodePayload;
import com.kidschedule.api.family.invite.InviteCodeStore;
import com.kidschedule.api.web.dto.CreateInviteRequest;
import com.kidschedule.api.web.dto.InviteCodeResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyInviteService {

	private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

	private final FamilyAccessService familyAccessService;
	private final InviteCodeStore inviteCodeStore;
	private final Duration inviteCodeTtl;
	private final SecureRandom secureRandom = new SecureRandom();

	public FamilyInviteService(
			FamilyAccessService familyAccessService,
			InviteCodeStore inviteCodeStore,
			Duration inviteCodeTtl) {
		this.familyAccessService = familyAccessService;
		this.inviteCodeStore = inviteCodeStore;
		this.inviteCodeTtl = inviteCodeTtl;
	}

	@Transactional(readOnly = true)
	public InviteCodeResponse createInviteCode(UUID familyId, UUID userId, CreateInviteRequest request) {
		requireMaster(familyId, userId);
		validateInviteRole(request.role());

		String code = generateUniqueCode();
		Instant expiresAt = Instant.now().plus(inviteCodeTtl);
		inviteCodeStore.save(
				code,
				new InviteCodePayload(familyId, request.role(), request.canEdit(), userId),
				inviteCodeTtl);

		return new InviteCodeResponse(code, request.role(), request.canEdit(), expiresAt);
	}

	private void requireMaster(UUID familyId, UUID userId) {
		var guardian = familyAccessService.requireGuardian(familyId, userId);
		if (guardian.getRole() != MemberRole.MASTER) {
			throw new IllegalArgumentException("Only the family master can create invite codes");
		}
	}

	private void validateInviteRole(MemberRole role) {
		if (role == MemberRole.MASTER) {
			throw new IllegalArgumentException("Cannot invite another master");
		}
	}

	private String generateUniqueCode() {
		for (int attempt = 0; attempt < 10; attempt++) {
			String code = randomCode();
			if (inviteCodeStore.find(code).isEmpty()) {
				return code;
			}
		}
		throw new IllegalStateException("Failed to generate invite code");
	}

	private String randomCode() {
		char[] chars = new char[8];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)];
		}
		return new String(chars);
	}
}
