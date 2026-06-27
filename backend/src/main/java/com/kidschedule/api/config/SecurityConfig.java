package com.kidschedule.api.config;

import com.kidschedule.api.auth.jwt.JwtAuthenticationFilter;
import com.kidschedule.api.auth.jwt.JwtProperties;
import com.kidschedule.api.auth.oauth.google.GoogleOAuthProperties;
import com.kidschedule.api.auth.oauth.kakao.KakaoOAuthProperties;
import com.kidschedule.api.auth.oauth.naver.NaverOAuthProperties;
import com.kidschedule.api.auth.oauth.OAuthSecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties({
	JwtProperties.class,
	OAuthSecurityProperties.class,
	KakaoOAuthProperties.class,
	NaverOAuthProperties.class,
	GoogleOAuthProperties.class
})
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/kakao/url").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/kakao/redirect").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/kakao/callback").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/naver/url").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/naver/redirect").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/naver/callback").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/google/url").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/google/redirect").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/google/callback").permitAll()
						.requestMatchers("/actuator/**").permitAll()
						.anyRequest().authenticated())
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
