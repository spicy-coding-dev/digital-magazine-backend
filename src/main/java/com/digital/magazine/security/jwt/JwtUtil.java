package com.digital.magazine.security.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final String SECRET_KEY = "ThisIsASecretKeyForJWTGenerationInIlayangudiNewsPostingApp1234"; // at least 32
	// bytes
	private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

	private final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 min
	private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30 days

	public String generateAccessToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String generateRefreshToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateToken(String token, String username) {
		return extractUsername(token).equals(username) && !isTokenExpired(token);
	}

	public boolean isTokenExpired(String token) {
		Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
				.getExpiration();
		return expiration.before(new Date());
	}

	public String extractUserFromHeader(String header) {
		if (header != null && header.startsWith("Bearer ")) {
			return extractUsername(header.substring(7));
		}
		return null;
	}

}
