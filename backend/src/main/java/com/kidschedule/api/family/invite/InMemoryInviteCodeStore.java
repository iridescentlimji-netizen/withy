package com.kidschedule.api.family.invite;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryInviteCodeStore implements InviteCodeStore {

	private record Entry(InviteCodePayload payload, long expiresAtEpochMs) {}

	private final Map<String, Entry> entries = new ConcurrentHashMap<>();

	@Override
	public void save(String code, InviteCodePayload payload, Duration ttl) {
		entries.put(code, new Entry(payload, System.currentTimeMillis() + ttl.toMillis()));
	}

	@Override
	public Optional<InviteCodePayload> find(String code) {
		Entry entry = entries.get(code);
		if (entry == null) {
			return Optional.empty();
		}
		if (System.currentTimeMillis() > entry.expiresAtEpochMs()) {
			entries.remove(code);
			return Optional.empty();
		}
		return Optional.of(entry.payload());
	}

	@Override
	public void delete(String code) {
		entries.remove(code);
	}
}
