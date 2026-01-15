package com.digital.magazine.analytics.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.analytics.dto.GuestArticleViewDto;
import com.digital.magazine.analytics.dto.GuestSessionDto;
import com.digital.magazine.analytics.service.GuestAnalyticsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/analytics/guest")
@RequiredArgsConstructor
@Slf4j
public class GuestAnalyticsController {

	private final GuestAnalyticsService service;

	@PostMapping("/session")
	public void trackGuestSession(@RequestBody GuestSessionDto dto, HttpServletRequest req) {

		service.trackGuestSession(dto.getGuestId(), dto.getDevice(), req.getRemoteAddr());
	}

	@PostMapping("/article-view")
	public void trackGuestArticle(@RequestBody GuestArticleViewDto dto) {

		service.trackGuestArticleView(dto.getGuestId(), dto.getArticleId(), dto.getTimeSpentSeconds());
	}
}
