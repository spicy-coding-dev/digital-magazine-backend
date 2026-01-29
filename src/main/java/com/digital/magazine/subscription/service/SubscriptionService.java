package com.digital.magazine.subscription.service;

import org.springframework.security.core.Authentication;

import com.digital.magazine.subscription.dto.BuySubscriptionRequest;

public interface SubscriptionService {

	public String buy(BuySubscriptionRequest req, Authentication auth);

}
