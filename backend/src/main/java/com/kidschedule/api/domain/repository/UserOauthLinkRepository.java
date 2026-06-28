package com.kidschedule.api.domain.repository;

import com.kidschedule.api.domain.entity.UserOauthLink;
import com.kidschedule.api.domain.enums.OAuthProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOauthLinkRepository extends JpaRepository<UserOauthLink, UUID> {

	Optional<UserOauthLink> findByOauthProviderAndOauthSubject(OAuthProvider oauthProvider, String oauthSubject);

	Optional<UserOauthLink> findByUserIdAndOauthProvider(UUID userId, OAuthProvider oauthProvider);

	List<UserOauthLink> findByUserIdOrderByOauthProviderAsc(UUID userId);
}
