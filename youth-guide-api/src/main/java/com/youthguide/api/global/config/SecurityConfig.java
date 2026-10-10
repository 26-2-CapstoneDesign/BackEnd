package com.youthguide.api.global.config;

import com.youthguide.api.global.security.CsrfCookieFilter;
import com.youthguide.api.global.security.JsonRememberMeServices;
import com.youthguide.api.global.security.MemberUserDetailsService;
import com.youthguide.api.global.security.SecurityErrorResponder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * 인증 방식: 세션(JSESSIONID) + 선택형 로그인 상태 유지(remember-me 쿠키, 14일).
 *
 * <p>CSRF: 쿠키 인증이라 CSRF 보호를 켠다. 서버가 XSRF-TOKEN 쿠키를 내려주면
 * 프론트는 상태를 바꾸는 요청(POST/PUT/PATCH/DELETE)에 X-XSRF-TOKEN 헤더로 그 값을 보낸다.
 * 아직 로그인 전인 회원가입·로그인 요청은 예외로 둔다.
 */
@Configuration
public class SecurityConfig {

	private static final int REMEMBER_ME_SECONDS = (int) Duration.ofDays(14).toSeconds();

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			SecurityContextRepository securityContextRepository,
			JsonRememberMeServices rememberMeServices,
			SecurityErrorResponder securityErrorResponder) throws Exception {
		http
				.csrf(csrf -> csrf
						.spa()
						.ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/members"))
				.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
				.cors(Customizer.withDefaults())
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.securityContext(context -> context.securityContextRepository(securityContextRepository))
				.rememberMe(rememberMe -> rememberMe
						.rememberMeServices(rememberMeServices)
						.key(rememberMeServices.getKey()))
				.logout(logout -> logout
						.logoutUrl("/api/v1/auth/logout")
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(securityErrorResponder)
						.accessDeniedHandler(securityErrorResponder))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/v1/members", "/api/v1/auth/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/members/availability").permitAll()
						.requestMatchers("/error").permitAll()
						// 다른 기능(자격증 조회 등)의 공개 API는 담당자가 여기에 permitAll로 추가한다.
						.anyRequest().authenticated());
		return http.build();
	}

	/** 저장값 예: {bcrypt}$2a$10$... 접두사로 알고리즘을 기록해 두면 나중에 알고리즘을 바꿔도 기존 회원이 로그인된다. */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(MemberUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public JsonRememberMeServices rememberMeServices(
			@Value("${app.auth.remember-me-key}") String rememberMeKey,
			MemberUserDetailsService userDetailsService) {
		JsonRememberMeServices services = new JsonRememberMeServices(rememberMeKey, userDetailsService);
		services.setTokenValiditySeconds(REMEMBER_ME_SECONDS);
		return services;
	}

	/** 프론트 개발 서버 주소는 app.cors.allowed-origins(쉼표 구분)로 설정한다. */
	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}
}
