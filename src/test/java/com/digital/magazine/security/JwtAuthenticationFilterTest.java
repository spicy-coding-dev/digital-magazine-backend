package com.digital.magazine.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.digital.magazine.security.jwt.JwtAuthenticationFilter;
import com.digital.magazine.security.jwt.JwtUtil;
import com.digital.magazine.security.service.CustomUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private CustomUserDetailsService userDetailsService;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private JwtAuthenticationFilter filter;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	// =========================
	// 1️⃣ Public endpoint → skip JWT
	// =========================
	@Test
	void publicEndpoint_shouldSkipJwtCheck() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/v1/auth/register");

		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	// =========================
	// 2️⃣ Valid JWT → authentication set
	// =========================
	@Test
	void validJwt_shouldAuthenticateUser() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/v1/books");
		request.addHeader("Authorization", "Bearer valid.jwt.token");

		MockHttpServletResponse response = new MockHttpServletResponse();

		User userDetails = new User("user@test.com", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));

		when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("user@test.com");

		when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

		when(jwtUtil.validateToken("valid.jwt.token", "user@test.com")).thenReturn(true);

		filter.doFilter(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getName());

		verify(filterChain).doFilter(request, response);
	}

	// =========================
	// 3️⃣ Expired JWT → 401
	// =========================
	@Test
	void expiredJwt_shouldReturnUnauthorized() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/v1/books");
		request.addHeader("Authorization", "Bearer expired.jwt.token");

		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtil.extractUsername("expired.jwt.token"))
				.thenThrow(new ExpiredJwtException(null, null, "JWT expired"));

		filter.doFilter(request, response, filterChain);

		assertEquals(401, response.getStatus());
		assertTrue(response.getContentAsString().contains("Please Login"));
	}

	@Test
	void invalidJwt_shouldReturnUnauthorized() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/v1/books");
		request.addHeader("Authorization", "Bearer invalid.jwt.token");

		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtil.extractUsername("invalid.jwt.token")).thenReturn("user@test.com");

		when(userDetailsService.loadUserByUsername("user@test.com"))
				.thenReturn(new org.springframework.security.core.userdetails.User("user@test.com", "pwd",
						Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));

		when(jwtUtil.validateToken("invalid.jwt.token", "user@test.com")).thenReturn(false); // 🔥 invalid

		filter.doFilter(request, response, filterChain);

		assertEquals(401, response.getStatus());
		assertTrue(response.getContentAsString().contains("Token இல்லை"));
	}

	@Test
	void authorizationHeaderMissing_shouldReturnUnauthorized() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/v1/books"); // secured endpoint
		// ❌ NO Authorization header

		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, filterChain);

		assertEquals(401, response.getStatus());
		assertTrue(response.getContentAsString().contains("Token இல்லை"));

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

}
