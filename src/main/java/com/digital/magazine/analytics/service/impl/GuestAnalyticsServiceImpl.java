package com.digital.magazine.analytics.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.digital.magazine.analytics.entity.GuestArticleView;
import com.digital.magazine.analytics.entity.GuestSession;
import com.digital.magazine.analytics.repository.GuestArticleViewRepository;
import com.digital.magazine.analytics.repository.GuestSessionRepository;
import com.digital.magazine.analytics.service.GuestAnalyticsService;
import com.digital.magazine.book.entity.Books;
import com.digital.magazine.book.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestAnalyticsServiceImpl implements GuestAnalyticsService {

	private final GuestSessionRepository guestSessionRepo;
	private final GuestArticleViewRepository guestArticleRepo;
	private final BookRepository bookRepo;

	@Override
	public void trackGuestSession(String guestId, String device, String ip) {

		GuestSession session = guestSessionRepo.findByGuestId(guestId).orElseGet(() -> {
			log.info("🆕 New guest session created | guestId={}", guestId);
			GuestSession gs = new GuestSession();
			gs.setGuestId(guestId);
			gs.setFirstVisitTime(LocalDateTime.now());
			gs.setIpAddress(ip);
			gs.setDevice(device);
			return gs;
		});

		session.setLastActiveTime(LocalDateTime.now());
		session.setDurationMinutes(
				Duration.between(session.getFirstVisitTime(), session.getLastActiveTime()).toMinutes());

		guestSessionRepo.save(session);

		log.info("👻 Guest session updated | guestId={} | duration={} mins", guestId, session.getDurationMinutes());
	}

	@Override
	public void trackGuestArticleView(String guestId, Long articleId, Long seconds) {

		Books book = bookRepo.findById(articleId).orElseThrow(() -> new RuntimeException("Article not found"));

		GuestArticleView view = new GuestArticleView();
		view.setGuestId(guestId);
		view.setArticle(book);
		view.setTimeSpentSeconds(seconds);

		guestArticleRepo.save(view);

		log.info("📖 Guest article view | guestId={} | articleId={} | time={}s", guestId, articleId, seconds);
	}
}
