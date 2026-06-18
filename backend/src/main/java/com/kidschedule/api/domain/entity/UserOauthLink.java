package com.kidschedule.api.domain.entity;

import com.kidschedule.api.common.BaseUuidEntity;
import com.kidschedule.api.domain.enums.OAuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_oauth_links")
public class UserOauthLink extends BaseUuidEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider", nullable = false, length = 20)
	private OAuthProvider oauthProvider;

	@Column(name = "oauth_subject", nullable = false, length = 255)
	private String oauthSubject;

	protected UserOauthLink() {
	}

	public UserOauthLink(User user, OAuthProvider oauthProvider, String oauthSubject) {
		this.user = user;
		this.oauthProvider = oauthProvider;
		this.oauthSubject = oauthSubject;
	}

	public User getUser() {
		return user;
	}

	public OAuthProvider getOauthProvider() {
		return oauthProvider;
	}

	public String getOauthSubject() {
		return oauthSubject;
	}
}
