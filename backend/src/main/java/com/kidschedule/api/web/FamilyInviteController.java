package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.family.FamilyInviteService;
import com.kidschedule.api.family.FamilyJoinService;
import com.kidschedule.api.web.dto.CreateInviteRequest;
import com.kidschedule.api.web.dto.FamilyJoinRequestResponse;
import com.kidschedule.api.web.dto.InviteCodeResponse;
import com.kidschedule.api.web.dto.JoinFamilyRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class FamilyInviteController {

	private final FamilyInviteService familyInviteService;
	private final FamilyJoinService familyJoinService;

	public FamilyInviteController(
			FamilyInviteService familyInviteService, FamilyJoinService familyJoinService) {
		this.familyInviteService = familyInviteService;
		this.familyJoinService = familyJoinService;
	}

	@PostMapping("/families/{familyId}/invite-codes")
	@ResponseStatus(HttpStatus.CREATED)
	public InviteCodeResponse createInviteCode(
			@PathVariable UUID familyId,
			@Valid @RequestBody CreateInviteRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return familyInviteService.createInviteCode(familyId, user.userId(), request);
	}

	@PostMapping("/join-requests")
	@ResponseStatus(HttpStatus.CREATED)
	public FamilyJoinRequestResponse submitJoinRequest(
			@Valid @RequestBody JoinFamilyRequest request, @AuthenticationPrincipal AuthenticatedUser user) {
		return familyJoinService.submitJoinRequest(user.userId(), request);
	}

	@GetMapping("/families/{familyId}/join-requests")
	public List<FamilyJoinRequestResponse> listPendingJoinRequests(
			@PathVariable UUID familyId, @AuthenticationPrincipal AuthenticatedUser user) {
		return familyJoinService.listPendingRequests(familyId, user.userId());
	}

	@PostMapping("/families/{familyId}/join-requests/{requestId}/approve")
	public FamilyJoinRequestResponse approveJoinRequest(
			@PathVariable UUID familyId,
			@PathVariable UUID requestId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return familyJoinService.approveJoinRequest(familyId, requestId, user.userId());
	}

	@PostMapping("/families/{familyId}/join-requests/{requestId}/reject")
	public FamilyJoinRequestResponse rejectJoinRequest(
			@PathVariable UUID familyId,
			@PathVariable UUID requestId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return familyJoinService.rejectJoinRequest(familyId, requestId, user.userId());
	}
}
