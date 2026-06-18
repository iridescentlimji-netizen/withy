package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.JoinStatus;
import com.kidschedule.api.domain.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "family_join_requests")
public class FamilyJoinRequest extends BaseUuidEntity {

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

	@Column(name = "invite_code_hash", nullable = false, length = 64)
	private String inviteCodeHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private JoinStatus status = JoinStatus.PENDING;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewed_by")
	private User reviewedBy;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

	protected FamilyJoinRequest() {
	}

	public FamilyJoinRequest(
			Family family,
			User user,
			MemberRole role,
			boolean canEdit,
			String inviteCodeHash) {
		this.family = family;
		this.user = user;
		this.role = role;
		this.canEdit = canEdit;
		this.inviteCodeHash = inviteCodeHash;
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

	public String getInviteCodeHash() {
		return inviteCodeHash;
	}

	public JoinStatus getStatus() {
		return status;
	}

	public User getReviewedBy() {
		return reviewedBy;
	}

	public Instant getReviewedAt() {
		return reviewedAt;
	}

	public void approve(User reviewer) {
		this.status = JoinStatus.APPROVED;
		this.reviewedBy = reviewer;
		this.reviewedAt = Instant.now();
	}

	public void reject(User reviewer) {
		this.status = JoinStatus.REJECTED;
		this.reviewedBy = reviewer;
		this.reviewedAt = Instant.now();
	}

	public void expire() {
		this.status = JoinStatus.EXPIRED;
	}
}
