package com.digital.magazine.analytics.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digital.magazine.analytics.dto.ArticleViewDto;
import com.digital.magazine.analytics.service.UserAnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/analytics/user")
@RequiredArgsConstructor
@Slf4j
public class UserAnalyticsController {

	private final UserAnalyticsService service;

	@PostMapping("/article-view")
	public void trackUserArticle(@RequestBody ArticleViewDto dto, Authentication auth) {

		service.trackArticleView(auth.getName(), dto.getArticleId(), dto.getTimeSpentSeconds());
	}
}
