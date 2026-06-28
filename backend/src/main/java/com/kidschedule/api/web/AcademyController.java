package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.family.AcademyService;
import com.kidschedule.api.web.dto.AcademyResponse;
import com.kidschedule.api.web.dto.CreateAcademyRequest;
import com.kidschedule.api.web.dto.UpdateAcademyRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/families/{familyId}/academies")
public class AcademyController {

	private final AcademyService academyService;

	public AcademyController(AcademyService academyService) {
		this.academyService = academyService;
	}

	@GetMapping
	public List<AcademyResponse> listAcademies(
			@PathVariable UUID familyId,
			@RequestParam(required = false) String query,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return academyService.listAcademies(familyId, user.userId(), query);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AcademyResponse createAcademy(
			@PathVariable UUID familyId,
			@Valid @RequestBody CreateAcademyRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return academyService.createAcademy(familyId, user.userId(), request);
	}

	@PutMapping("/{academyId}")
	public AcademyResponse updateAcademy(
			@PathVariable UUID familyId,
			@PathVariable UUID academyId,
			@Valid @RequestBody UpdateAcademyRequest request,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return academyService.updateAcademy(familyId, academyId, user.userId(), request);
	}

	@DeleteMapping("/{academyId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAcademy(
			@PathVariable UUID familyId,
			@PathVariable UUID academyId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		academyService.deleteAcademy(familyId, academyId, user.userId());
	}
}
