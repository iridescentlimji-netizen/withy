package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "child_credentials")
public class ChildCredential extends BaseEntity {

	@Id
	@Column(name = "child_id")
	private UUID childId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId
	@JoinColumn(name = "child_id")
	private Child child;

	@Column(name = "pin_hash", nullable = false)
	private String pinHash;

	@Column(name = "failed_attempts", nullable = false)
	private short failedAttempts;

	@Column(name = "locked_until")
	private Instant lockedUntil;

	protected ChildCredential() {
	}

	public ChildCredential(Child child, String pinHash) {
		this.child = child;
		this.pinHash = pinHash;
	}

	public UUID getChildId() {
		return childId;
	}

	public Child getChild() {
		return child;
	}

	public String getPinHash() {
		return pinHash;
	}

	public void setPinHash(String pinHash) {
		this.pinHash = pinHash;
	}

	public short getFailedAttempts() {
		return failedAttempts;
	}

	public void setFailedAttempts(short failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public Instant getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(Instant lockedUntil) {
		this.lockedUntil = lockedUntil;
	}
}
