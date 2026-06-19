package com.kidschedule.api.auth.oauth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StringUtils;

public final class OAuthRedirectSupport {

	private OAuthRedirectSupport() {
	}

	public static String resolveReturnUri(String requestedReturnUri) {
		if (!StringUtils.hasText(requestedReturnUri)) {
			return null;
		}
		if (requestedReturnUri.startsWith("exp://")
				|| requestedReturnUri.startsWith("kid-schedule://")) {
			return requestedReturnUri;
		}
		throw new IllegalArgumentException("Invalid return URI");
	}

	public static String buildRedirectBridge(OAuthStateStore oAuthStateStore, String code, String state) {
		OAuthState oauthState = oAuthStateStore.peekState(state);
		if (!StringUtils.hasText(oauthState.returnUri())) {
			return """
					<!DOCTYPE html>
					<html lang="ko">
					<body>
					<p>로그인 처리 중입니다. 잠시만 기다려 주세요.</p>
					</body>
					</html>
					""";
		}

		String separator = oauthState.returnUri().contains("?") ? "&" : "?";
		String target = oauthState.returnUri()
				+ separator
				+ "code="
				+ URLEncoder.encode(code, StandardCharsets.UTF_8)
				+ "&state="
				+ URLEncoder.encode(state, StandardCharsets.UTF_8);
		String escapedTarget = target.replace("&", "&amp;").replace("\"", "&quot;");
		String jsTarget = target.replace("\\", "\\\\").replace("'", "\\'");

		return """
				<!DOCTYPE html>
				<html lang="ko">
				<head>
				<meta charset="utf-8">
				<meta http-equiv="refresh" content="0;url=%s">
				</head>
				<body>
				<p>앱으로 돌아가는 중...</p>
				<p><a id="app-link" href="%s">앱으로 돌아가기</a></p>
				<script>
				window.location.replace('%s');
				document.getElementById('app-link').click();
				</script>
				</body>
				</html>
				"""
				.formatted(escapedTarget, escapedTarget, jsTarget);
	}
}
