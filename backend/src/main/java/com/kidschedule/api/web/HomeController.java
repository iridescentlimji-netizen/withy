package com.kidschedule.api.web;

import com.kidschedule.api.auth.AuthenticatedUser;
import com.kidschedule.api.schedule.HomeService;
import com.kidschedule.api.web.dto.HomeResponse;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

	private final HomeService homeService;

	public HomeController(HomeService homeService) {
		this.homeService = homeService;
	}

	@GetMapping
	public HomeResponse getHome(
			@RequestParam UUID familyId, @AuthenticationPrincipal AuthenticatedUser user) {
		return homeService.getHome(familyId, user.userId());
	}
}
