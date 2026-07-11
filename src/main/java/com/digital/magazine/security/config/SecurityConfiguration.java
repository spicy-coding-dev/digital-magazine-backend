//package com.digital.magazine.security.config;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import com.digital.magazine.security.jwt.JwtAuthenticationFilter;
//
//import jakarta.servlet.http.HttpServletResponse;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//	@Autowired
//	private JwtAuthenticationFilter jwtFilter;
//
//	@Bean
//	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.csrf(csrf -> csrf.disable()).cors(withDefaults()) // ✅ enable CORS support
//				.authorizeHttpRequests(auth -> auth
//						.requestMatchers("/api/v1/auth/register", "/api/v1/auth/verify-email",
//								"/api/v1/auth/user-login", "/api/v1/auth/refresh", "/api/v1/auth/forgot-password",
//								"/api/v1/auth/reset-password", "/api/v1/super-admin/verify-email",
//								"/api/v1/subscriptions/getplans", "/api/v1/user/**", "/swagger-ui.html",
//								"/swagger-ui/**", "/v3/api-docs/**", "/api/v1/analytics/guest/**", "/actuator/**",
//								"/api/v1/manage/verify-email")
//						.permitAll() // login
//										// &
//										// register
//										// open
//						.requestMatchers("/api/v1/subscription/**", "/api/v1/addresses/**", "/api/v1/payments/**")
//						.hasRole("USER") // news
//						.requestMatchers("/api/v1/admin/**", "/api/v1/subscriptions/**", "/api/v1/manage/create-user",
//								"/api/v1/email/**")
//						.hasRole("ADMIN") // news
//						.requestMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")
//						.requestMatchers("/api/v1/auth/logout", "/api/v1/analytics/user/**", "/api/v1/users/**")
//						.authenticated().anyRequest().denyAll())
//				// ✅ Correctly placed session management for JWT (stateless)
//				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//		// ✅ Add JWT filter before UsernamePasswordAuthenticationFilter
//		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//		// Custom access denied handling
//		http.exceptionHandling(handling -> handling
//
//				.authenticationEntryPoint((request, response, authException) -> {
//
//					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//					response.setContentType("application/json;charset=UTF-8");
//
//					response.getWriter().write("""
//							{
//							    "message":"தயவுசெய்து உள்நுழையவும் / Please Login"
//							}
//							""");
//				})
//
//				.accessDeniedHandler((request, response, accessDeniedException) -> {
//
//					response.setContentType("application/json;charset=UTF-8");
//
//					String path = request.getRequestURI();
//					String message;
//
//					if (path.startsWith("/api/v1/super-admin/")) {
//						message = "சூப்பர் அட்மின் தரவை அணுக உங்களுக்கு அனுமதி இல்லை";
//					} else if (path.startsWith("/api/v1/admin/") || path.startsWith("/api/v1/subscriptions/")
//							|| path.startsWith("/api/v1/manage/create-user") || path.startsWith("/api/v1/email/")) {
//
//						message = "அட்மின் தரவை அணுக உங்களுக்கு அனுமதி இல்லை";
//
//					} else if (path.startsWith("/api/v1/user/") || path.startsWith("/api/v1/subscription/")
//							|| path.startsWith("/api/v1/addresses/") || path.startsWith("/api/v1/payments")) {
//
//						message = "இந்த API-க்கு USER role தேவை";
//
//					} else {
//
//						message = "உங்களுக்கு இந்த resource-ஐ அணுக அனுமதி இல்லை";
//
//					}
//
//					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//
//					response.getWriter().write("""
//							{
//							    "message":"%s"
//							}
//							""".formatted(message));
//
//				}));
//		return http.build();
//	}
//
//	@Bean
//	PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
//
//	@Bean
//	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//		return authConfig.getAuthenticationManager();
//	}
//
//}

package com.digital.magazine.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.digital.magazine.security.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Autowired
	private JwtAuthenticationFilter jwtFilter;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).cors(withDefaults())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						// ==========================
						// PUBLIC APIs
						// ==========================
						.requestMatchers("/api/v1/auth/register", "/api/v1/auth/verify-email",
								"/api/v1/auth/user-login", "/api/v1/auth/refresh", "/api/v1/auth/forgot-password",
								"/api/v1/auth/reset-password", "/api/v1/super-admin/verify-email",
								"/api/v1/subscriptions/getplans", "/api/v1/manage/verify-email", "/api/v1/user/**",
								"/api/v1/analytics/guest/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
								"/actuator/**")
						.permitAll()

						.requestMatchers(HttpMethod.GET, "/api/v1/comments/book/**").permitAll()

						// ==========================
						// USER APIs
						// ==========================
						.requestMatchers(HttpMethod.POST, "/api/v1/comments/book/**").hasRole("USER")

						.requestMatchers(HttpMethod.DELETE, "/api/v1/comments/**").hasRole("USER")

						.requestMatchers("/api/v1/subscription/**", "/api/v1/addresses/**", "/api/v1/payments/**")
						.hasRole("USER")

						// ==========================
						// ADMIN
						// ==========================
						.requestMatchers("/api/v1/admin/**", "/api/v1/subscriptions/**", "/api/v1/manage/create-user",
								"/api/v1/email/**", "/api/v1/comments/admin/**")
						.hasRole("ADMIN")

						// ==========================
						// SUPER ADMIN
						// ==========================
						.requestMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")

						// ==========================
						// AUTHENTICATED
						// ==========================
						.requestMatchers("/api/v1/auth/logout", "/api/v1/auth/me", "/api/v1/analytics/user/**",
								"/api/v1/users/**")
						.authenticated()

						.anyRequest().authenticated())

				.exceptionHandling(exception -> exception

						// ==========================
						// 401 Unauthorized
						// ==========================
						.authenticationEntryPoint((request, response, ex) -> {

							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType("application/json;charset=UTF-8");

							response.getWriter().write("""
									{
									  "status":401,
									  "message":"தயவுசெய்து உள்நுழையவும் / Please Login"
									}
									""");
						})

						// ==========================
						// 403 Forbidden
						// ==========================
						.accessDeniedHandler((request, response, ex) -> {

							response.setStatus(HttpStatus.FORBIDDEN.value());
							response.setContentType("application/json;charset=UTF-8");

							String path = request.getRequestURI();
							String message;

							if (path.startsWith("/api/v1/super-admin/")) {

								message = "சூப்பர் அட்மின் தரவை அணுக உங்களுக்கு அனுமதி இல்லை";

							} else if (path.startsWith("/api/v1/admin/") || path.startsWith("/api/v1/subscriptions/")
									|| path.startsWith("/api/v1/manage/create-user")
									|| path.startsWith("/api/v1/email/")
									|| path.startsWith("/api/v1/comments/admin/")) {

								message = "அட்மின் தரவை அணுக உங்களுக்கு அனுமதி இல்லை";

							} else if (path.startsWith("/api/v1/subscription/") || path.startsWith("/api/v1/addresses/")
									|| path.startsWith("/api/v1/payments")
									|| path.startsWith("/api/v1/comments/book/")) {

								message = "இந்த API-யை அணுக USER Role தேவை";

							} else {

								message = "இந்த Resource-ஐ அணுக உங்களுக்கு அனுமதி இல்லை";

							}

							response.getWriter().write("""
									{
									  "status":403,
									  "message":"%s"
									}
									""".formatted(message));
						})

				);

		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
