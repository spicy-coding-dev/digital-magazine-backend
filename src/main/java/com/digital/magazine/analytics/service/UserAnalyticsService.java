package com.digital.magazine.analytics.service;

public interface UserAnalyticsService {

	public void loginSuccess(String email);

	public void logout(String email);

	public void trackArticleView(String email, Long articleId, Long seconds);

}
