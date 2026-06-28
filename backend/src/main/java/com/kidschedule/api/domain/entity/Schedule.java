package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.enums.SubjectCategory;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "series_id")
	private ScheduleSeries series;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "child_id", nullable = false)
	private Child child;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "academy_id")
	private Academy academy;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_type", nullable = false, length = 30)
	private ScheduleType scheduleType = ScheduleType.OTHER;

	@Enumerated(EnumType.STRING)
	@Column(name = "subject_category", length = 30)
	private SubjectCategory subjectCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pickup_guardian_id")
	private User pickupGuardian;

	@Column(name = "start_at", nullable = false)
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	private Instant endAt;

	@Column(nullable = false)
	private boolean cancelled = false;

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

	public ScheduleSeries getSeries() {
		return series;
	}

	public void setSeries(ScheduleSeries series) {
		this.series = series;
	}

	public Child getChild() {
		return child;
	}

	public Academy getAcademy() {
		return academy;
	}

	public void setAcademy(Academy academy) {
		this.academy = academy;
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

	public SubjectCategory getSubjectCategory() {
		return subjectCategory;
	}

	public void setSubjectCategory(SubjectCategory subjectCategory) {
		this.subjectCategory = subjectCategory;
	}

	public User getPickupGuardian() {
		return pickupGuardian;
	}

	public void setPickupGuardian(User pickupGuardian) {
		this.pickupGuardian = pickupGuardian;
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

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public User getCreatedBy() {
		return createdBy;
	}
}
