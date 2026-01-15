package com.digital.magazine.analytics.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.digital.magazine.analytics.entity.ArticleView;
import com.digital.magazine.analytics.entity.UserSession;
import com.digital.magazine.analytics.repository.ArticleViewRepository;
import com.digital.magazine.analytics.repository.UserSessionRepository;
import com.digital.magazine.analytics.service.UserAnalyticsService;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalyticsServiceImpl implements UserAnalyticsService {

	private final UserSessionRepository userSessionRepo;
	private final ArticleViewRepository articleViewRepo;
	private final BookRepository bookRepo;
	private final UserRepository userRepo;

	@Override
	public void loginSuccess(String email) {

		User user = userRepo.findByEmail(email).orElseThrow();

		UserSession session = new UserSession();
		session.setUser(user);
		session.setLoginTime(LocalDateTime.now());

		userSessionRepo.save(session);

		log.info("🔐 User login | user={}", email);
	}

	@Override
	public void logout(String email) {

		User user = userRepo.findByEmail(email).orElseThrow();

		UserSession session = userSessionRepo.findTopByUserOrderByLoginTimeDesc(user).orElseThrow();

		session.setLogoutTime(LocalDateTime.now());
		session.setDurationMinutes(Duration.between(session.getLoginTime(), session.getLogoutTime()).toMinutes());

		userSessionRepo.save(session);

		log.info("🔓 User logout | user={} | duration={} mins", email, session.getDurationMinutes());
	}

	@Override
	public void trackArticleView(String email, Long articleId, Long seconds) {

		User user = userRepo.findByEmail(email).orElseThrow();
		Books book = bookRepo.findById(articleId).orElseThrow();

		ArticleView view = new ArticleView();
		view.setUser(user);
		view.setArticle(book);
		view.setViewTime(LocalDateTime.now());
		view.setTimeSpentSeconds(seconds);

		articleViewRepo.save(view);

		log.info("📘 User article view | user={} | articleId={} | time={}s", email, articleId, seconds);
	}
}
