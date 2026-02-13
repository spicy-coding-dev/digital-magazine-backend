package com.digital.magazine.common.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

	private LoginAttemptService service;
	private final String KEY = "user@test.com";

	@BeforeEach
	void setup() {
		service = new LoginAttemptService();
	}

	// ✅ INITIAL STATE
	@Test
	void initiallyNoCaptchaAndNotBlocked() {

		assertFalse(service.shouldShowCaptcha(KEY));
		assertFalse(service.isBlocked(KEY));
	}

	// ❌ 1–2 FAILED ATTEMPTS → NO CAPTCHA
	@Test
	void captchaShouldNotShowBeforeThreshold() {

		service.loginFailed(KEY); // 1
		service.loginFailed(KEY); // 2

		assertFalse(service.shouldShowCaptcha(KEY));
		assertFalse(service.isBlocked(KEY));
	}

	// 🔐 3–4 FAILED ATTEMPTS → CAPTCHA REQUIRED
	@Test
	void captchaShouldShowAfterThreshold() {

		service.loginFailed(KEY); // 1
		service.loginFailed(KEY); // 2
		service.loginFailed(KEY); // 3

		assertTrue(service.shouldShowCaptcha(KEY));
		assertFalse(service.isBlocked(KEY));

		service.loginFailed(KEY); // 4

		assertTrue(service.shouldShowCaptcha(KEY));
		assertFalse(service.isBlocked(KEY));
	}

	// 🚫 5 FAILED ATTEMPTS → BLOCK USER
	@Test
	void userShouldBeBlockedAfterMaxAttempts() {

		service.loginFailed(KEY); // 1
		service.loginFailed(KEY); // 2
		service.loginFailed(KEY); // 3
		service.loginFailed(KEY); // 4
		service.loginFailed(KEY); // 5

		assertTrue(service.isBlocked(KEY));
		assertFalse(service.shouldShowCaptcha(KEY));
	}

	// ✅ SUCCESS LOGIN → RESET ATTEMPTS
	@Test
	void loginSuccessShouldResetAttempts() {

		service.loginFailed(KEY); // 1
		service.loginFailed(KEY); // 2
		service.loginFailed(KEY); // 3

		assertTrue(service.shouldShowCaptcha(KEY));

		service.loginSucceeded(KEY);

		assertFalse(service.shouldShowCaptcha(KEY));
		assertFalse(service.isBlocked(KEY));
	}
}
