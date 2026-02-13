package com.digital.magazine.security.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.digital.magazine.security.service.CustomUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		log.debug("🔍 JWT Filter triggered | path={}", path);

		if (path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/verify-email")
				|| path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/forgot-password")
				|| path.equals("/api/v1/auth/reset-password") || path.equals("/api/v1/super-admin/verify-email")
				|| path.equals("/api/v1/subscriptions/getplans") || path.startsWith("/api/v1/analytics/guest")
				|| path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator")
				|| path.equals("/api/v1/manage/verify-email")) {

			log.debug("🔓 JWT skipped for public endpoint | path={}", path);
			filterChain.doFilter(request, response); // skip JWT check
			return;
		}

		final String authHeader = request.getHeader("Authorization");
//		String username = null;
//		String jwt = null;

		try {
			if (authHeader != null && authHeader.startsWith("Bearer ")) {

				log.debug("🔐 Authorization header found");

				String jwt = authHeader.substring(7);
				String username = jwtUtil.extractUsername(jwt); // ⚠️ can throw ExpiredJwtException

				log.debug("👤 Username extracted from JWT | username={}", username);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

						log.info("✅ JWT validated | user={}", username);

						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
					} else {
						log.warn("❌ JWT validation failed | user={}", username);
						throw new RuntimeException(
								"Token இல்லை அல்லது தவறான Token ஆதலால் உள்நுழைவு பக்கம்(Login page) வழியாக மீண்டும் உள்நுழையவும்");
					}
				}
			} else {
				log.debug("⚠️ No Authorization header present");
			}

			filterChain.doFilter(request, response);

		} catch (ExpiredJwtException e) {
			// ⚡ Handle JWT expiration here
			log.warn("⏰ JWT expired | path={}", path);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(
					"{\"message\":\"நீங்கள் யார் என்று என்னால் அறிய முடியவில்லை, தயவுசெய்து மீண்டும் உள்நுழையவும்/Please Login\"}");
		} catch (RuntimeException e) {
			log.error("🚫 Invalid JWT / authentication error | path={}", path, e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(
					"{\"message\":\"Token இல்லை அல்லது தவறான Token ஆதலால் உள்நுழைவு பக்கம்(Login page) வழியாக மீண்டும் உள்நுழையவும்\"}");
		}

	}

}
