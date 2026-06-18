package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseUuidEntity {

	@Column(nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false, length = 10)
	private AccountType accountType;

	@OneToMany(mappedBy = "user")
	private List<UserOauthLink> oauthLinks = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<FamilyGuardian> guardianMemberships = new ArrayList<>();

	protected User() {
	}

	public User(String nickname, AccountType accountType) {
		this.nickname = nickname;
		this.accountType = accountType;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public List<UserOauthLink> getOauthLinks() {
		return oauthLinks;
	}

	public List<FamilyGuardian> getGuardianMemberships() {
		return guardianMemberships;
	}
}
