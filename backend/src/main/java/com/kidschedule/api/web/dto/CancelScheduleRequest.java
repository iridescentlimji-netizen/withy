package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.CancelScope;

public record CancelScheduleRequest(CancelScope scope) {

	public CancelScheduleRequest {
		if (scope == null) {
			scope = CancelScope.OCCURRENCE;
		}
	}
}
