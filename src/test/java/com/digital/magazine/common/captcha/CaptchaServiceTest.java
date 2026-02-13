package com.digital.magazine.common.captcha;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

	@Mock
	private WebClient.Builder webClientBuilder;

	@Mock
	private WebClient webClient;

	@Mock
	private RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private RequestBodySpec requestBodySpec;

	@Mock
	private ResponseSpec responseSpec;

	@InjectMocks
	private CaptchaService captchaService;

	@BeforeEach
	void setup() {

		when(webClientBuilder.build()).thenReturn(webClient);

		// post() → RequestBodyUriSpec
		when(webClient.post()).thenReturn(requestBodyUriSpec);

		// uri(...) → RequestBodySpec
		when(requestBodyUriSpec.uri(anyString(), any(), any())).thenReturn(requestBodySpec);

		// retrieve() → ResponseSpec
		when(requestBodySpec.retrieve()).thenReturn(responseSpec);
	}

	// ✅ SUCCESS = true
	@Test
	void validate_successTrue() {

		when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
				.thenReturn(Mono.just(Map.of("success", true)));

		boolean result = captchaService.validate("dummy-captcha");

		assertTrue(result);
	}

	// ❌ SUCCESS = false
	@Test
	void validate_successFalse() {

		when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
				.thenReturn(Mono.just(Map.of("success", false)));

		boolean result = captchaService.validate("dummy-captcha");

		assertFalse(result);
	}

	// 🔥 EXCEPTION CASE
	@Test
	void validate_exceptionHandled() {

		when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
				.thenThrow(new RuntimeException("Google API down"));

		boolean result = captchaService.validate("dummy-captcha");

		assertFalse(result);
	}
}
