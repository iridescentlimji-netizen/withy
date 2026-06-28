package com.kidschedule.api.web.dto;

import com.kidschedule.api.domain.enums.SubjectCategory;
import java.util.UUID;

public record AcademyResponse(
		UUID id, String name, String phone, SubjectCategory defaultSubjectCategory, String memo) {
}
