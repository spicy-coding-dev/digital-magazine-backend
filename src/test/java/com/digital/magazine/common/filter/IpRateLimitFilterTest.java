package com.digital.magazine.common.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class IpRateLimitFilterTest {

	private IpRateLimitFilter filter;

	private HttpServletRequest request;
	private HttpServletResponse response;
	private FilterChain filterChain;

	@BeforeEach
	void setup() {
		filter = new IpRateLimitFilter();

		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		filterChain = mock(FilterChain.class);

		when(request.getRemoteAddr()).thenReturn("127.0.0.1");
	}

	// ✅ ALLOWED REQUEST (within rate limit)
	@Test
	void shouldAllowRequestWithinLimit() throws Exception {

		filter.doFilter(request, response, filterChain);

		// chain must be called
		verify(filterChain, times(1)).doFilter(request, response);

		// response status should NOT be 429
		verify(response, never()).setStatus(429);
	}

	// ❌ BLOCKED REQUEST (rate limit exceeded)
	@Test
	void shouldBlockRequestWhenLimitExceeded() throws Exception {

		// prepare writer FIRST
		StringWriter writer = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(writer));

		// consume 10 allowed requests
		for (int i = 0; i < 10; i++) {
			filter.doFilter(request, response, filterChain);
		}

		// 11th request → blocked
		filter.doFilter(request, response, filterChain);

		verify(response).setStatus(429);
		verify(filterChain, times(10)).doFilter(request, response);

		String responseBody = writer.toString();

		assertFalse(responseBody.isBlank());
		assertTrue(responseBody.contains("ஒரு (1) நிமிடத்தில்"));
	}

	// ✅ DIFFERENT IP → DIFFERENT BUCKET
	@Test
	void differentIpShouldHaveDifferentBucket() throws Exception {

		when(request.getRemoteAddr()).thenReturn("192.168.1.10");

		filter.doFilter(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
	}

}
