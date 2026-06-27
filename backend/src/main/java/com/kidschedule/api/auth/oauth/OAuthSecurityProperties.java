package com.kidschedule.api.auth.oauth;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.oauth.security")
public class OAuthSecurityProperties {

	private String allowedReturnUris = "";
	private String allowedExpHosts = "127.0.0.1,localhost";

	public String getAllowedReturnUris() {
		return allowedReturnUris;
	}

	public void setAllowedReturnUris(String allowedReturnUris) {
		this.allowedReturnUris = allowedReturnUris;
	}

	public String getAllowedExpHosts() {
		return allowedExpHosts;
	}

	public void setAllowedExpHosts(String allowedExpHosts) {
		this.allowedExpHosts = allowedExpHosts;
	}

	public List<String> getExactAllowedReturnUris() {
		return splitCsv(allowedReturnUris);
	}

	public List<String> getAllowedExpHostnames() {
		return splitCsv(allowedExpHosts);
	}

	private List<String> splitCsv(String value) {
		if (!StringUtils.hasText(value)) {
			return List.of();
		}
		return Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
	}
}
