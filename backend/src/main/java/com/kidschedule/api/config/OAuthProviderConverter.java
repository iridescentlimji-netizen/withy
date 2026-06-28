package com.kidschedule.api.config;

import com.kidschedule.api.domain.enums.OAuthProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OAuthProviderConverter implements Converter<String, OAuthProvider> {

	@Override
	public OAuthProvider convert(String source) {
		if (source == null || source.isBlank()) {
			throw new IllegalArgumentException("OAuth provider is required");
		}
		return OAuthProvider.valueOf(source.trim().toUpperCase());
	}
}
