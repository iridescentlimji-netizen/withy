package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "families")
public class Family extends BaseUuidEntity {

	@Column(nullable = false, length = 50)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@OneToMany(mappedBy = "family")
	private List<FamilyGuardian> guardians = new ArrayList<>();

	@OneToMany(mappedBy = "family")
	private List<Child> children = new ArrayList<>();

	@OneToMany(mappedBy = "family")
	private List<FamilyJoinRequest> joinRequests = new ArrayList<>();

	@OneToMany(mappedBy = "family")
	private List<Academy> academies = new ArrayList<>();

	protected Family() {
	}

	public Family(String name, User createdBy) {
		this.name = name;
		this.createdBy = createdBy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public List<FamilyGuardian> getGuardians() {
		return guardians;
	}

	public List<Child> getChildren() {
		return children;
	}

	public List<FamilyJoinRequest> getJoinRequests() {
		return joinRequests;
	}

	public List<Academy> getAcademies() {
		return academies;
	}
}
