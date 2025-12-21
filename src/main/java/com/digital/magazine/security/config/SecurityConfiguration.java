package com.digital.magazine.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Autowired
	private JwtAuthenticationFilter jwtFilter;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(withDefaults()) // ✅ enable CORS support
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
						.permitAll() // login & register open
						.requestMatchers("/news/**", "/user/**", "/auth/update-dev-token")
						.hasAnyRole("USER", "SUPER_ADMIN") // news protected
						.requestMatchers("/super/v1/**", "/auth/update-dev-token").hasRole("SUPER_ADMIN")
						.requestMatchers("/auth/logout").authenticated().anyRequest().denyAll())
				// ✅ Correctly placed session management for JWT (stateless)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// ✅ Add JWT filter before UsernamePasswordAuthenticationFilter
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		// Custom access denied handling
		http.exceptionHandling(handling -> handling.accessDeniedHandler((request, response, accessDeniedException) -> {
			response.setContentType("application/json;charset=UTF-8");
			String path = request.getRequestURI();
			String message;

			if (path.startsWith("/super/v1/")) {
				message = "சூப்பர் அட்மின் தரவை அணுக உங்களுக்கு அனுமதி இல்லை";
			} else if (path.startsWith("/news/") || path.startsWith("/user/")) {
				message = "இந்த API-க்கு உங்களுக்கு USER role தேவை";
			} else {
				message = "உங்களுக்கு இந்த resource-ஐ அணுக அனுமதி இல்லை";
			}

			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("{\"message\":\"" + message + "\"}");
		}));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

}
