package com.digital.magazine.common.security;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginAttemptService {

	private final int CAPTCHA_THRESHOLD = 3;
	private final int MAX_ATTEMPT = 5;
	private final Cache<String, Integer> attemptsCache;

	public LoginAttemptService() {
		this.attemptsCache = Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES) // unblock after 15 mins
				.maximumSize(10000).build();
	}

	public void loginSucceeded(String key) {
		log.info("✅ Login success | resetting attempts | key={}", key);
		attemptsCache.invalidate(key);
	}

	public boolean shouldShowCaptcha(String key) {
		Integer attempts = attemptsCache.getIfPresent(key);
		return attempts != null && attempts >= CAPTCHA_THRESHOLD && attempts < MAX_ATTEMPT;
	}

	public void loginFailed(String key) {
		Integer attempts = attemptsCache.getIfPresent(key);
		if (attempts == null)
			attempts = 0;
		attempts++;
		attemptsCache.put(key, attempts);
		log.warn("❌ Login failed | key={} | attempts={}", key, attempts);

		if (attempts == CAPTCHA_THRESHOLD) {
			log.warn("🧩 CAPTCHA threshold reached | key={} | attempts={}", key, attempts);
		}

		if (attempts >= MAX_ATTEMPT) {
			log.error("🚫 Account temporarily blocked | key={} | attempts={}", key, attempts);
		}
	}

	// 🚫 Block check
	public boolean isBlocked(String key) {
		Integer attempts = attemptsCache.getIfPresent(key);
		boolean blocked = attempts != null && attempts >= MAX_ATTEMPT;

		log.debug("🚫 Block check | key={} | attempts={} | blocked={}", key, attempts, blocked);

		return blocked;
	}

}
