package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "family_guardians")
public class FamilyGuardian extends BaseUuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "family_id", nullable = false)
	private Family family;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MemberRole role;

	@Column(name = "can_edit", nullable = false)
	private boolean canEdit;

	protected FamilyGuardian() {
	}

	public FamilyGuardian(Family family, User user, MemberRole role, boolean canEdit) {
		this.family = family;
		this.user = user;
		this.role = role;
		this.canEdit = canEdit;
	}

	public Family getFamily() {
		return family;
	}

	public User getUser() {
		return user;
	}

	public MemberRole getRole() {
		return role;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setRole(MemberRole role) {
		this.role = role;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
}
