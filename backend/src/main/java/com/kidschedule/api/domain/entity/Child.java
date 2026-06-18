package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "children")
public class Child extends BaseUuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "family_id", nullable = false)
	private Family family;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Column(name = "birth_year", nullable = false)
	private short birthYear;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "can_edit", nullable = false)
	private boolean canEdit;

	@Column(name = "app_enabled", nullable = false)
	private boolean appEnabled;

	@OneToOne(mappedBy = "child", fetch = FetchType.LAZY)
	private ChildCredential credential;

	@OneToMany(mappedBy = "child")
	private List<ChildDeviceSession> deviceSessions = new ArrayList<>();

	@OneToMany(mappedBy = "child")
	private List<Schedule> schedules = new ArrayList<>();

	protected Child() {
	}

	public Child(Family family, String nickname, short birthYear) {
		this.family = family;
		this.nickname = nickname;
		this.birthYear = birthYear;
	}

	public Family getFamily() {
		return family;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public short getBirthYear() {
		return birthYear;
	}

	public User getUser() {
		return user;
	}

	public void linkUser(User childUser) {
		this.user = childUser;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public boolean isAppEnabled() {
		return appEnabled;
	}

	public void setAppEnabled(boolean appEnabled) {
		this.appEnabled = appEnabled;
	}

	public ChildCredential getCredential() {
		return credential;
	}

	public List<ChildDeviceSession> getDeviceSessions() {
		return deviceSessions;
	}

	public List<Schedule> getSchedules() {
		return schedules;
	}
}
