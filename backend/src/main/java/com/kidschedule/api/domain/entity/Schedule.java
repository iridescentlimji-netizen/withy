package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.ScheduleType;
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
@Table(name = "schedules")
public class Schedule extends BaseUuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "child_id", nullable = false)
	private Child child;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_type", nullable = false, length = 30)
	private ScheduleType scheduleType = ScheduleType.OTHER;

	@Column(name = "start_at", nullable = false)
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	private Instant endAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	protected Schedule() {
	}

	public Schedule(
			Child child,
			String title,
			String description,
			ScheduleType scheduleType,
			Instant startAt,
			Instant endAt,
			User createdBy) {
		this.child = child;
		this.title = title;
		this.description = description;
		this.scheduleType = scheduleType;
		this.startAt = startAt;
		this.endAt = endAt;
		this.createdBy = createdBy;
	}

	public Child getChild() {
		return child;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public void setStartAt(Instant startAt) {
		this.startAt = startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	public void setEndAt(Instant endAt) {
		this.endAt = endAt;
	}

	public User getCreatedBy() {
		return createdBy;
	}
}
