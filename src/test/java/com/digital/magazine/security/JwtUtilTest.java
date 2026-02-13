package com.digital.magazine.security;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digital.magazine.security.jwt.JwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setup() {
		jwtUtil = new JwtUtil();
	}

	@Test
	void generateAccessToken_shouldCreateValidToken() {
		String token = jwtUtil.generateAccessToken("user@test.com");

		assertNotNull(token);
		assertEquals("user@test.com", jwtUtil.extractUsername(token));
	}

	@Test
	void generateRefreshToken_shouldCreateValidToken() {
		String token = jwtUtil.generateRefreshToken("user@test.com");

		assertNotNull(token);
		assertEquals("user@test.com", jwtUtil.extractUsername(token));
	}

	@Test
	void extractUsername_shouldReturnUsername() {
		String token = jwtUtil.generateAccessToken("user@test.com");

		String username = jwtUtil.extractUsername(token);

		assertEquals("user@test.com", username);
	}

	@Test
	void validateToken_validToken_shouldReturnTrue() {
		String token = jwtUtil.generateAccessToken("user@test.com");

		boolean valid = jwtUtil.validateToken(token, "user@test.com");

		assertTrue(valid);
	}

	@Test
	void validateToken_wrongUsername_shouldReturnFalse() {
		String token = jwtUtil.generateAccessToken("user@test.com");

		boolean valid = jwtUtil.validateToken(token, "another@test.com");

		assertFalse(valid);
	}

	@Test
	void isTokenExpired_expiredToken_shouldReturnTrue() throws Exception {

		// 🔥 Create expired token manually
		Field keyField = JwtUtil.class.getDeclaredField("key");
		keyField.setAccessible(true);

		var key = (java.security.Key) keyField.get(jwtUtil);

		String expiredToken = Jwts.builder().setSubject("user@test.com")
				.setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
				.setExpiration(new Date(System.currentTimeMillis() - 1000)) // expired
				.signWith(key, SignatureAlgorithm.HS256).compact();

		assertTrue(jwtUtil.isTokenExpired(expiredToken));
	}

	@Test
	void extractUserFromHeader_validHeader_shouldReturnUsername() {
		String token = jwtUtil.generateAccessToken("user@test.com");
		String header = "Bearer " + token;

		String username = jwtUtil.extractUserFromHeader(header);

		assertEquals("user@test.com", username);
	}

	@Test
	void extractUserFromHeader_invalidHeader_shouldReturnNull() {
		String username = jwtUtil.extractUserFromHeader("InvalidHeader");

		assertNull(username);
	}

	@Test
	void extractUsername_invalidToken_shouldThrowException() {
		assertThrows(Exception.class, () -> {
			jwtUtil.extractUsername("invalid.jwt.token");
		});
	}
}
