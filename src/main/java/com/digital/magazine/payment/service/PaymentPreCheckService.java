package com.digital.magazine.payment.service;

import org.springframework.security.core.Authentication;

import com.digital.magazine.subscription.dto.BuySubscriptionRequest;

public interface PaymentPreCheckService {

	public void preCheckSingleBook(Authentication auth, Long bookId);

	public void preCheckSubscription(Authentication auth, BuySubscriptionRequest req);

}
