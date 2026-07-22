package ditda.backend.global.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import ditda.backend.global.jwt.filter.JwtTokenFilter;
import ditda.backend.global.jwt.handler.JwtAccessDeniedHandler;
import ditda.backend.global.jwt.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenFilter jwtTokenFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			// csrf 비활성화
			.csrf(AbstractHttpConfigurer::disable)
			// form login 비활성화
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			// cors 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			// 세션 stateless
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// 인증/인가 예외 처리
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			// api 접근 권한 설정
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/instructors/auth/signup").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/designers/auth/signup",
					"/api/v1/designers/auth/signup/portfolio/presigned-url").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/admin/auth/login").permitAll()
				.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/prometheus").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/internal/watermarks/callback").permitAll()
				.requestMatchers("/api/v1/instructors/**").hasRole("INSTRUCTOR")
				.requestMatchers("/api/v1/designers/**").hasRole("DESIGNER")
				.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public FilterRegistrationBean<JwtTokenFilter> jwtTokenFilterRegistration(JwtTokenFilter filter) {
		FilterRegistrationBean<JwtTokenFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);

		return registration;
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(List.of(
			"http://localhost:3000",
			"http://localhost:5173",
			"https://ditda.kr",
			"https://www.ditda.kr",
			"https://api.ditda.kr"
		));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
