package com.kidschedule.api.family.invite;

import com.kidschedule.api.config.RedisConfig;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisInviteCodeStore implements InviteCodeStore {

	private final StringRedisTemplate redisTemplate;

	public RedisInviteCodeStore(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void save(String code, InviteCodePayload payload, Duration ttl) {
		String value = encode(payload);
		redisTemplate.opsForValue().set(RedisConfig.INVITE_CODE_KEY_PREFIX + code, value, ttl);
	}

	@Override
	public Optional<InviteCodePayload> find(String code) {
		String value = redisTemplate.opsForValue().get(RedisConfig.INVITE_CODE_KEY_PREFIX + code);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(decode(value));
	}

	@Override
	public void delete(String code) {
		redisTemplate.delete(RedisConfig.INVITE_CODE_KEY_PREFIX + code);
	}

	private String encode(InviteCodePayload payload) {
		return payload.familyId()
				+ "|"
				+ payload.role().name()
				+ "|"
				+ payload.canEdit()
				+ "|"
				+ payload.createdByUserId();
	}

	private InviteCodePayload decode(String value) {
		String[] parts = value.split("\\|", 4);
		return new InviteCodePayload(
				UUID.fromString(parts[0]),
				com.kidschedule.api.domain.enums.MemberRole.valueOf(parts[1]),
				Boolean.parseBoolean(parts[2]),
				UUID.fromString(parts[3]));
	}
}
