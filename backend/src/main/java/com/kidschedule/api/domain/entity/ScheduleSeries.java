package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.RecurrenceType;
import com.kidschedule.api.domain.enums.ScheduleType;
import com.kidschedule.api.domain.enums.SubjectCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_series")
public class ScheduleSeries extends BaseUuidEntity {

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
	private ScheduleType scheduleType;

	@Enumerated(EnumType.STRING)
	@Column(name = "subject_category", length = 30)
	private SubjectCategory subjectCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pickup_guardian_id")
	private User pickupGuardian;

	@Enumerated(EnumType.STRING)
	@Column(name = "recurrence_type", nullable = false, length = 20)
	private RecurrenceType recurrenceType;

	@Column(name = "days_of_week")
	private Short daysOfWeek;

	@Column(name = "day_of_month")
	private Short dayOfMonth;

	@Column(name = "anchor_date")
	private LocalDate anchorDate;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Column(name = "effective_from", nullable = false)
	private LocalDate effectiveFrom;

	@Column(name = "effective_until")
	private LocalDate effectiveUntil;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@OneToMany(mappedBy = "series")
	private List<Schedule> schedules = new ArrayList<>();

	protected ScheduleSeries() {
	}

	public Child getChild() {
		return child;
	}

	public Academy getAcademy() {
		return academy;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public SubjectCategory getSubjectCategory() {
		return subjectCategory;
	}

	public User getPickupGuardian() {
		return pickupGuardian;
	}

	public RecurrenceType getRecurrenceType() {
		return recurrenceType;
	}

	public Short getDaysOfWeek() {
		return daysOfWeek;
	}

	public Short getDayOfMonth() {
		return dayOfMonth;
	}

	public LocalDate getAnchorDate() {
		return anchorDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public LocalDate getEffectiveFrom() {
		return effectiveFrom;
	}

	public LocalDate getEffectiveUntil() {
		return effectiveUntil;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}

	public void setSubjectCategory(SubjectCategory subjectCategory) {
		this.subjectCategory = subjectCategory;
	}

	public void setPickupGuardian(User pickupGuardian) {
		this.pickupGuardian = pickupGuardian;
	}

	public void setAcademy(Academy academy) {
		this.academy = academy;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public void setEffectiveUntil(LocalDate effectiveUntil) {
		this.effectiveUntil = effectiveUntil;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Child child;
		private Academy academy;
		private String title;
		private String description;
		private ScheduleType scheduleType;
		private SubjectCategory subjectCategory;
		private User pickupGuardian;
		private RecurrenceType recurrenceType;
		private Short daysOfWeek;
		private Short dayOfMonth;
		private LocalDate anchorDate;
		private LocalTime startTime;
		private LocalTime endTime;
		private LocalDate effectiveFrom;
		private LocalDate effectiveUntil;
		private User createdBy;

		public Builder child(Child child) {
			this.child = child;
			return this;
		}

		public Builder academy(Academy academy) {
			this.academy = academy;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder scheduleType(ScheduleType scheduleType) {
			this.scheduleType = scheduleType;
			return this;
		}

		public Builder subjectCategory(SubjectCategory subjectCategory) {
			this.subjectCategory = subjectCategory;
			return this;
		}

		public Builder pickupGuardian(User pickupGuardian) {
			this.pickupGuardian = pickupGuardian;
			return this;
		}

		public Builder recurrenceType(RecurrenceType recurrenceType) {
			this.recurrenceType = recurrenceType;
			return this;
		}

		public Builder daysOfWeek(Short daysOfWeek) {
			this.daysOfWeek = daysOfWeek;
			return this;
		}

		public Builder dayOfMonth(Short dayOfMonth) {
			this.dayOfMonth = dayOfMonth;
			return this;
		}

		public Builder anchorDate(LocalDate anchorDate) {
			this.anchorDate = anchorDate;
			return this;
		}

		public Builder startTime(LocalTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public Builder endTime(LocalTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public Builder effectiveFrom(LocalDate effectiveFrom) {
			this.effectiveFrom = effectiveFrom;
			return this;
		}

		public Builder effectiveUntil(LocalDate effectiveUntil) {
			this.effectiveUntil = effectiveUntil;
			return this;
		}

		public Builder createdBy(User createdBy) {
			this.createdBy = createdBy;
			return this;
		}

		public ScheduleSeries build() {
			ScheduleSeries series = new ScheduleSeries();
			series.child = child;
			series.academy = academy;
			series.title = title;
			series.description = description;
			series.scheduleType = scheduleType;
			series.subjectCategory = subjectCategory;
			series.pickupGuardian = pickupGuardian;
			series.recurrenceType = recurrenceType;
			series.daysOfWeek = daysOfWeek;
			series.dayOfMonth = dayOfMonth;
			series.anchorDate = anchorDate;
			series.startTime = startTime;
			series.endTime = endTime;
			series.effectiveFrom = effectiveFrom;
			series.effectiveUntil = effectiveUntil;
			series.createdBy = createdBy;
			return series;
		}
	}
}
