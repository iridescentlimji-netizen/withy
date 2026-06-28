package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.SubjectCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "academies")
public class Academy extends BaseUuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "family_id", nullable = false)
	private Family family;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 30)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "default_subject_category", length = 30)
	private SubjectCategory defaultSubjectCategory;

	@Column(length = 500)
	private String memo;

	protected Academy() {
	}

	public Academy(Family family, String name, String phone, SubjectCategory defaultSubjectCategory, String memo) {
		this.family = family;
		this.name = name;
		this.phone = phone;
		this.defaultSubjectCategory = defaultSubjectCategory;
		this.memo = memo;
	}

	public Family getFamily() {
		return family;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public SubjectCategory getDefaultSubjectCategory() {
		return defaultSubjectCategory;
	}

	public void setDefaultSubjectCategory(SubjectCategory defaultSubjectCategory) {
		this.defaultSubjectCategory = defaultSubjectCategory;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
