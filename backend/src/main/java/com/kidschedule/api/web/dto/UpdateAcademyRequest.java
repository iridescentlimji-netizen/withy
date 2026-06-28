package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.SubjectCategory;
import jakarta.validation.constraints.Size;

public record UpdateAcademyRequest(
		@Size(max = 100) String name,
		@Size(max = 30) String phone,
		SubjectCategory defaultSubjectCategory,
		@Size(max = 500) String memo) {
}
