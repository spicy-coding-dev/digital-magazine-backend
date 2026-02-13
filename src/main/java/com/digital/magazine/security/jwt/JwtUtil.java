package com.digital.magazine.security.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

	private final String SECRET_KEY = "ThisIsASecretKeyForJWTGenerationInIlayangudiNewsPostingApp1234"; // at least 32
	// bytes
	private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

	private final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 min
	private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30 days

	public String generateAccessToken(String username) {

		log.debug("🔐 Generating ACCESS token | user={}", username);

		String token = Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
				.signWith(key, SignatureAlgorithm.HS256).compact();

		log.info("✅ Access token generated | user={}", username);
		return token;
	}

	public String generateRefreshToken(String username) {

		log.debug("🔄 Generating REFRESH token | user={}", username);

		String token = Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
				.signWith(key, SignatureAlgorithm.HS256).compact();

		log.info("✅ Refresh token generated | user={}", username);
		return token;
	}

	public String extractUsername(String token) {

		log.debug("🔍 Extracting username from token");

		String username = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();

		log.debug("👤 Username extracted = {}", username);
		return username;
	}

	public boolean validateToken(String token, String username) {

		log.debug("🧪 Validating token | expectedUser={}", username);

		try {
			String tokenUser = extractUsername(token);

			boolean valid = tokenUser.equals(username) && !isTokenExpired(token);

			log.info("✅ Token validation result | user={} | valid={}", username, valid);
			return valid;

		} catch (Exception e) {
			log.warn("❌ Token validation failed | reason={}", e.getMessage());
			return false;
		}
	}

	public boolean isTokenExpired(String token) {
		try {
			Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
					.getExpiration();

			boolean expired = expiration.before(new Date());

			log.debug("⏰ Token expiry check | expired={}", expired);
			return expired;

		} catch (ExpiredJwtException e) {
			log.warn("⛔ Token expired at {}", e.getClaims().getExpiration());
			return true; // 🔥 THIS IS THE KEY
		}
	}

	public String extractUserFromHeader(String header) {

		if (header != null && header.startsWith("Bearer ")) {
			log.debug("📥 Extracting token from Authorization header");
			return extractUsername(header.substring(7));
		}

		log.warn("⚠️ Authorization header missing or invalid");
		return null;
	}

}
