package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.family.ChildService;
import com.kidschedule.api.family.FamilyService;
import com.kidschedule.api.web.dto.ChildResponse;
import com.kidschedule.api.web.dto.CreateChildRequest;
import com.kidschedule.api.web.dto.CreateFamilyRequest;
import com.kidschedule.api.web.dto.FamilyResponse;
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
@RequestMapping("/api/v1/families")
public class FamilyController {

	private final FamilyService familyService;
	private final ChildService childService;

	public FamilyController(FamilyService familyService, ChildService childService) {
		this.familyService = familyService;
		this.childService = childService;
	}

	@GetMapping
	public List<FamilyResponse> listMyFamilies(@AuthenticationPrincipal AuthenticatedUser user) {
		return familyService.listMyFamilies(user.userId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FamilyResponse createFamily(
			@Valid @RequestBody CreateFamilyRequest request, @AuthenticationPrincipal AuthenticatedUser user) {
		return familyService.createFamily(user.userId(), request);
	}

	@GetMapping("/{familyId}")
	public FamilyResponse getFamily(
			@PathVariable UUID familyId, @AuthenticationPrincipal AuthenticatedUser user) {
		return familyService.getFamily(familyId, user.userId());
	}

	@GetMapping("/{familyId}/children")
	public List<ChildResponse> listChildren(
			@PathVariable UUID familyId, @AuthenticationPrincipal AuthenticatedUser user) {
		return childService.listChildren(familyId, user.userId());
	}

	@PostMapping("/{familyId}/children")
	@ResponseStatus(HttpStatus.CREATED)
	public ChildResponse createChild(
			@PathVariable UUID familyId,
			@Valid @RequestBody CreateChildRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return childService.createChild(familyId, user.userId(), request);
	}
}
