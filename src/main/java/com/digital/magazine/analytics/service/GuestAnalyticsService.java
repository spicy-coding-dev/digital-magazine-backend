package com.digital.magazine.analytics.service;

public interface GuestAnalyticsService {

	public void trackGuestSession(String guestId, String device, String ip);

	public void trackGuestArticleView(String guestId, Long articleId, Long seconds);

}
