package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "child_device_sessions")
public class ChildDeviceSession extends BaseUuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "child_id", nullable = false)
	private Child child;

	@Column(name = "device_token_hash", nullable = false)
	private String deviceTokenHash;

	@Column(name = "device_name", length = 100)
	private String deviceName;

	@Column(name = "last_active_at", nullable = false)
	private Instant lastActiveAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	protected ChildDeviceSession() {
	}

	public ChildDeviceSession(
			Child child,
			String deviceTokenHash,
			String deviceName,
			Instant lastActiveAt,
			Instant expiresAt) {
		this.child = child;
		this.deviceTokenHash = deviceTokenHash;
		this.deviceName = deviceName;
		this.lastActiveAt = lastActiveAt;
		this.expiresAt = expiresAt;
	}

	public Child getChild() {
		return child;
	}

	public String getDeviceTokenHash() {
		return deviceTokenHash;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public Instant getLastActiveAt() {
		return lastActiveAt;
	}

	public void touch(Instant lastActiveAt) {
		this.lastActiveAt = lastActiveAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
