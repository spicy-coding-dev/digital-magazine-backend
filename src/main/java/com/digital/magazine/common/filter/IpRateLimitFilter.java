package com.digital.magazine.common.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class IpRateLimitFilter implements Filter {

	private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

	private Bucket createNewBucket() {
		Bandwidth limit = Bandwidth.builder().capacity(10) // bucket can hold up to 10 tokens
				.refillGreedy(10, Duration.ofMinutes(1)) // refills 10 tokens per minute (smooth)
				.build();
		return Bucket.builder().addLimit(limit).build();
	}

	private Bucket resolveBucket(String ip) {
		return cache.computeIfAbsent(ip, k -> createNewBucket());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String ip = ((HttpServletRequest) request).getRemoteAddr();
		Bucket bucket = resolveBucket(ip);
		if (bucket.tryConsume(1)) {
			chain.doFilter(request, response);
		} else {
			((HttpServletResponse) response).setStatus(429);
			response.getWriter().write(
					"இந்த IP-யிலிருந்து ஒரு (1) நிமிடத்தில் பத்து (10) முறைகளுக்கு மேற்பட்ட அழுத்தங்களை செய்துள்ளீர்கள். உங்கள் பாதுகாப்பிற்காக 1 நிமிடம் கழித்து மீண்டும் முயற்சிக்கவும்.");
		}
	}

}
