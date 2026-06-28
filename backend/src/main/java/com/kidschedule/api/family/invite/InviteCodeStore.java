package com.kidschedule.api.family.invite;

import java.time.Duration;
import java.util.Optional;

public interface InviteCodeStore {

	void save(String code, InviteCodePayload payload, Duration ttl);

	Optional<InviteCodePayload> find(String code);

	void delete(String code);
}
