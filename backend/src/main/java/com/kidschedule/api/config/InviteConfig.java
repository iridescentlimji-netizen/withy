package com.kidschedule.api.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InviteConfig {

	@Value("${app.invite.ttl-hours:24}")
	private long inviteTtlHours;

	@Bean
	public Duration inviteCodeTtl() {
		return Duration.ofHours(inviteTtlHours);
	}
}
