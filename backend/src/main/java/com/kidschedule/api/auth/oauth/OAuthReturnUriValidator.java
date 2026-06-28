package com.kidschedule.api.auth.oauth;

import com.kidschedule.api.domain.enums.OAuthProvider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OAuthReturnUriValidator {

	private static final Pattern EXP_RETURN_URI_PATTERN =
			Pattern.compile("^exp://([^/?#]+)/--/oauth/(kakao|naver|google)(?:/link)?$");

	private final OAuthSecurityProperties securityProperties;

	public OAuthReturnUriValidator(OAuthSecurityProperties securityProperties) {
		this.securityProperties = securityProperties;
	}

	public String resolveReturnUri(String requestedReturnUri, OAuthProvider provider) {
		if (!StringUtils.hasText(requestedReturnUri)) {
			return null;
		}

		String returnUri = requestedReturnUri.trim();
		if (securityProperties.getExactAllowedReturnUris().contains(returnUri)) {
			return returnUri;
		}
		if (matchesKidScheduleReturnUri(returnUri, provider)) {
			return returnUri;
		}
		if (matchesAllowedExpReturnUri(returnUri, provider)) {
			return returnUri;
		}

		throw new IllegalArgumentException("Return URI is not allowed");
	}

	private boolean matchesKidScheduleReturnUri(String returnUri, OAuthProvider provider) {
		return returnUri.equals(buildKidScheduleReturnUri(provider))
				|| returnUri.equals(buildKidScheduleLinkReturnUri(provider));
	}

	private boolean matchesAllowedExpReturnUri(String returnUri, OAuthProvider provider) {
		Matcher matcher = EXP_RETURN_URI_PATTERN.matcher(returnUri);
		if (!matcher.matches()) {
			return false;
		}

		String providerSlug = provider.name().toLowerCase();
		if (!providerSlug.equals(matcher.group(2))) {
			return false;
		}

		String host = extractHost(matcher.group(1));
		return securityProperties.getAllowedExpHostnames().contains(host);
	}

	private String extractHost(String hostPort) {
		int colonIndex = hostPort.lastIndexOf(':');
		if (colonIndex <= 0) {
			return hostPort;
		}
		return hostPort.substring(0, colonIndex);
	}

	public static String buildKidScheduleReturnUri(OAuthProvider provider) {
		return "kid-schedule://oauth/" + provider.name().toLowerCase();
	}

	public static String buildKidScheduleLinkReturnUri(OAuthProvider provider) {
		return buildKidScheduleReturnUri(provider) + "/link";
	}
}
